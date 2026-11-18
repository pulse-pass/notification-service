#!/usr/bin/env bash
set -eo pipefail

MODE="${1:-test}"
if [ "$MODE" != "test" ] && [ "$MODE" != "development" ]; then
    echo "Invalid mode '$MODE'. Usage: ./service-start.sh [test|development]" >&2
    exit 1
fi

cd "$(dirname "$0")"

docker network inspect pulse-pass >/dev/null 2>&1 || docker network create pulse-pass

if [ ! -f .env ]; then
    cp .env.example .env
fi

set -a
. ./.env

SUFFIX="_$(echo "$MODE" | tr '[:lower:]' '[:upper:]')_URL"
for var in $(grep -oE '^[A-Z0-9_]+(_TEST_URL|_DEVELOPMENT_URL)' .env | sort -u); do
    case "$var" in
        *"$SUFFIX")
            name="${var%"$SUFFIX"}"
            export "${name}_URL=${!var}"
            ;;
    esac
done
set +a

if [ "$MODE" = "test" ]; then
    PROJECT="notification-service-test"
    PROFILE_ARGS=(--profile test)
    export SERVICE_PREFIX="$PROJECT"
    export MYSQL_HOST="$PROJECT-mysql"
    export NOTIFICATION_EVENT_RETRY_DELAY=500

    ./service-stop.sh test
else
    PROJECT="notification-service"
    PROFILE_ARGS=()
    SHARED_MYSQL="pulse-pass-mysql"
    export SERVICE_PREFIX="$PROJECT"
    export MYSQL_HOST="$SHARED_MYSQL"

    for shared in pulse-pass-mysql pulse-pass-redis pulse-pass-kafka; do
        HEALTH="$(docker inspect -f '{{.State.Health.Status}}' "$shared" 2>/dev/null || true)"
        if [ "$HEALTH" != "healthy" ]; then
            echo "The shared container '$shared' is not running or not healthy. Start the pulse-pass-infrastructure first (infrastructure-start.sh in that repository)." >&2
            exit 1
        fi
    done

    ./service-stop.sh development

    {
        echo "CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';"
        echo "ALTER USER '${MYSQL_USER}'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';"
        cat database.sql
        echo "GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'%';"
    } | docker exec -i -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" "$SHARED_MYSQL" mysql -uroot
fi

docker compose -p "$PROJECT" "${PROFILE_ARGS[@]}" up -d --build --wait

echo "notification-service consumer is up in $MODE mode (no HTTP endpoint)"
