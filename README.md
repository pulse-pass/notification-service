# notification-service

Serviço de notificação do PulsePass: consome os pedidos de envio publicados pelos demais serviços no tópico `notification_event` e envia os e-mails. Hoje trata a aprovação e a reprovação do cadastro de uma organização (`ORGANIZER_APPROVED` e `ORGANIZER_REJECTED`). O serviço não tem API HTTP e roda um único contêiner, o `notification_event-consumer`, que é a mesma aplicação Spring Boot iniciada com o perfil `notification_event-consumer`, sem servidor web.

Cada mensagem leva uma cópia dos dados necessários para montar o e-mail, então o serviço não consulta os outros. O assunto, o modelo e as variáveis exigidas de cada tipo ficam na tabela `notification_event_type`; o serviço não acrescenta variáveis: tudo o que o modelo usa, inclusive `recipient_name`, vem do produtor em `email.variables`, e o link do backoffice é fixo no próprio modelo; o consumidor grava o evento e o e-mail no MySQL (schema `notification`), bloqueia a chave do evento no Redis (`notification-service:notification-event:<notification_event_key>`), envia pela API REST do Mailtrap Email Sandbox e registra cada tentativa. Uma mensagem repetida cujo e-mail já está como `SENT` ou `FAILED` é ignorada.

A mensagem tem o envelope `event_type`, `event_key` e `event_data`. O `event_type` do envelope é a ação sobre a entidade e o consumidor só trata `NOTIFICATION_EVENT_CREATION`. O tipo da notificação vai em `event_data.notification_event_type` (junto de `notification_event_key`, `person_key`, `origin`, `origin_key`, `receiver_contact` e `email.variables`), e é só do `event_data` que o serviço monta o pedido de envio.

Se o Mailtrap recusar o e-mail (status 400 ou 422, por exemplo destinatário ou campos inválidos), a recusa é um erro mapeado, inclusive quando vem sem corpo: a tentativa é gravada e o e-mail fica como `FAILED`, sem nova tentativa. Toda tentativa de envio, inclusive a bem-sucedida, grava uma linha em `email_message_attempt`, e a linha guarda apenas o `error_code`, que é o código da exceção de domínio na falha (`NTF00001` para a recusa, `NTF00006` para a indisponibilidade e `NTF00007` para a falha inesperada) e nulo no sucesso; o id da mensagem devolvido pelo Mailtrap vai para `email_message.external_message_key`. Se o Mailtrap falhar de outra forma (qualquer outro status, como 401, 403, 404, 429 ou 5xx, resposta sem corpo ou falha de conexão), a tentativa também é gravada, com o código `NTF00006`, e a mensagem vai para o tópico de retry `notification_event_retry` (1 partição, retenção de 7 dias, como o `notification_event`), sendo tentada de novo a cada intervalo fixo (`NOTIFICATION_EVENT_RETRY_DELAY`, 60000 ms no modo `development` e 500 ms no modo `test`, que o `service-start.sh` fixa para os testes de integração); ao chegar a 5 tentativas gravadas para o e-mail, ele fica como `FAILED` e a mensagem sai da fila, sem nova tentativa. Uma falha inesperada na chamada ao gateway de e-mail (qualquer outra exceção) grava a tentativa com o código `NTF00007` e marca o e-mail como `FAILED`, sem nova tentativa. Os demais erros não mapeados (JSON quebrado, campo obrigatório ausente, origem desconhecida, tipo de notificação sem linha em `notification_event_type`, variável exigida ausente, erro de banco, chave do evento bloqueada) não são tratados, e a mensagem não é descartada nem trava as demais: vai para o mesmo tópico de retry e é tentada de novo até 7 dias (`app.notification-event.retry.max-attempts`, 10081 por padrão: a tentativa original mais 10080 novas tentativas, 7 dias com o intervalo padrão). Não há DLQ. Cada tópico tem 1 partição e é consumido por uma thread (constantes `PARTITIONS` e `CONCURRENCY` do `NotificationEventKafkaConfig`); para processar em paralelo, as duas sobem juntas.

## Subir localmente

Para subir o serviço só é necessário o Docker: a imagem compila a aplicação com o JDK que ela mesma traz. O Java 21 só é necessário para rodar o `./mvnw` (lint e testes) fora do container.

```bash
./infrastructure/service-start.sh [test|development]
./infrastructure/service-stop.sh [test|development]
```

O script cria a rede `pulse-pass` e o `infrastructure/.env` (a partir do `.env.example`) quando não existem, derruba o ambiente do mesmo modo e sobe o consumidor. O modo padrão é `test`, também no `service-stop.sh`. Para cada par `<NAME>_TEST_URL` / `<NAME>_DEVELOPMENT_URL` do `.env` (`KAFKA`, `REDIS` e `MAILTRAP`), o script exporta `<NAME>_URL` conforme o modo.

Modo `test`: o serviço é autossuficiente e sobe um MySQL, um Redis, um Kafka e um MockServer próprios, além do consumidor, no projeto `notification-service-test`. As portas publicadas são MySQL em `localhost:3309`, Redis em `localhost:6381`, Kafka em `localhost:9095` e MockServer em `localhost:1083`. É o modo usado pela CI e pelos testes de integração. O MockServer substitui a API do Mailtrap (`MAILTRAP_TEST_URL`) e os testes registram nele as respostas de sucesso e de recusa. O `service-stop.sh test` remove containers, volumes e imagens locais desse modo.

Modo `development`: sobe só o consumidor (container `notification-service-notification_event-consumer`, projeto `notification-service`), usando o MySQL, o Redis e o Kafka compartilhados (`pulse-pass-mysql`, `pulse-pass-redis` e `pulse-pass-kafka`) e o Mailtrap Email Sandbox real (`https://sandbox.api.mailtrap.io`). Por isso o `pulse-pass-infrastructure` precisa estar de pé e saudável; se algum deles não estiver, o script termina com erro pedindo para subi-lo. No MySQL compartilhado o script cria, como root, o usuário do `.env` com permissão apenas no schema `notification` e aplica o `database.sql`. O `database.sql` é idempotente, então rodar o script de novo não duplica nada, mas também não recria o que já existe: mudanças em tabelas só aparecem no development depois de reiniciar a infraestrutura compartilhada (que apaga o MySQL) e subir o serviço de novo. O `service-stop.sh development` derruba só o consumidor e não mexe nos contêineres compartilhados.

Quem já tem um `infrastructure/.env` de uma versão anterior deve apagá-lo para que o script o recrie a partir do `.env.example`. Usuário, senha e schema do `.env` não podem conter aspas nem barra invertida, pois são interpolados no SQL do modo `development`. Valores com espaços ou `<`, como o `MAILTRAP_API_TOKEN` do exemplo, ficam entre aspas simples.

Os dois modos podem rodar ao mesmo tempo. Como não há URL HTTP, o consumidor saudável (o healthcheck confere um arquivo que ele grava ao receber as partições do tópico) é o sinal de que está de pé.

## Configurar o Mailtrap

No modo `development` os e-mails vão para um sandbox real do Mailtrap, e é lá que são conferidos. Para configurar:

1. Crie uma conta gratuita em `https://mailtrap.io` e, em Email Sandbox, crie um projeto e uma caixa de entrada (sandbox).
2. Gere o token em Settings > API Tokens (com acesso à caixa de entrada) e coloque em `MAILTRAP_API_TOKEN` no `infrastructure/.env`.
3. Pegue o id do sandbox (o número que aparece na URL da caixa de entrada, `https://mailtrap.io/inboxes/<id>/messages`) e coloque em `MAILTRAP_SANDBOX_ID`.
4. Ajuste `MAIL_FROM_EMAIL` e `MAIL_FROM_NAME`, que formam o remetente.

O serviço envia um `POST` em `/api/send/<MAILTRAP_SANDBOX_ID>` com o header `Api-Token` e o corpo `from`, `to`, `subject`, `text`, `html` e `category` (o nome do modelo). No modo `test` não há conta nem token reais: o MockServer em `http://localhost:1083` responde no lugar da API, com o `MAILTRAP_API_TOKEN` e o `MAILTRAP_SANDBOX_ID` do `.env.example` (valores de teste).

## Lint e testes

```bash
./mvnw checkstyle:check        # Checkstyle
./mvnw test                    # testes unitários (relatório JaCoCo em target/site/jacoco)
./mvnw test -Pintegration      # testes de integração (exigem o compose no ar)
```

Os testes de integração publicam mensagens no Kafka (`localhost:9095`) e conferem o resultado pelas requisições recebidas pelo MockServer (`localhost:1083`); nunca acessam o banco.
