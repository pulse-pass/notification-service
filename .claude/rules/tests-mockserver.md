---
description: MockServer expectations and MockTools
paths:
  - "src/test/**"
---

# Tests with MockServer

- **NTF-190** The Mailtrap API is played by the MockServer of the compose (`test` mode, port 1083), driven by `MockTools` in `src/test/java/.../common/`; its address comes from `MOCKSERVER_BASE_URL` and `MOCKSERVER_PORT` in `BaseIntegrationTest` (defaults `http://localhost` and `1083`), and `MockTools` is identical to the other repositories except for the package and the default port; the service reaches it through `MAILTRAP_TEST_URL`.
- **NTF-191** `MockTools` offers `clearMocker()`, `addExpectation(mockCode)`, `addExpectation(mockCode, pathParameters)`, `addExpectation(mockCode, pathParameters, expectationChanges)` and `retrieveRequests(method, path)`.
- **NTF-192** Each external route has a default expectation in `src/test/resources/expectations/<api>/<method>-<name>.json`, in the MockServer format: `httpRequest` with `{param}` placeholders in the path (`/api/send/{sandbox_id}`) and `httpResponse` with a realistic default body (`mailtrap/post-send-email.json`, status 200 with `message_ids`).
- **NTF-193** Other responses of the same route are variants in their own file (`<method>-<name>_<variant>.json`, e.g. `mailtrap/post-send-email_bad_request.json`, status 400 with `errors`).
- **NTF-194** `addExpectation` loads the template, sets `pathParameters` on the request and applies `expectationChanges` to the response: `payload` overrides keys of the body, `status_code` the status and `headers` the headers.
- **NTF-195** Every integration test starts with `MockTools.clearMocker()` and registers only the expectations it needs, with the values (sandbox id, e-mail addresses) declared in the test itself or in `BaseIntegrationTest`.
- **NTF-196** No expectations in `@BeforeAll`, none shared between tests, and no `priority` or body matching to tell tests apart.
- **NTF-197** Integration tests run sequentially because they share the MockServer; clearing it also clears the recorded requests, which is what makes `retrieveRequests` count only the requests of the current test.
- **NTF-198** The result of a send is checked by the request the MockServer received (`retrieveRequests("POST", "/api/send/<sandbox_id>")`: recipient, subject, category, `text` and `html`), and the absence of a new send by waiting with Awaitility `during` and checking that the count did not grow.
- **NTF-199** `MockTools` has no route-specific methods and no constants other than its configurable address: paths live in the templates, and tests write the external paths inline in `retrieveRequests`.
- **NTF-201** A variant that must fail only a few times carries `times` (`remainingTimes`, `unlimited: false`) in the template (`mailtrap/post-send-email_server_error.json`, status 500 with `errors`, `remainingTimes` 2; `post-send-email_bad_request.json` has `remainingTimes` 1 and `post-send-email_service_unavailable.json` has 5, the send limit); the test registers it first and the default success expectation after it (together or once the variant is spent), so MockServer answers with the variant until it is spent and with the default afterwards, without clearing the mocker in the middle of a test.
