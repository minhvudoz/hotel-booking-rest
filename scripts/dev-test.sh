#!/usr/bin/env bash
# Dựng stack Docker riêng (cổng 18080, volume tạm, JVM chạy UTC), chạy Postman collection bằng Newman rồi dọn sạch.
# KEEP=1 ./scripts/dev-test.sh để giữ lại stack sau khi chạy (debug).
set -euo pipefail
cd "$(dirname "$0")/.."
export APP_PORT=18080 APP_TZ=UTC
PROJECT=hotel-test
cleanup() { docker compose -p "$PROJECT" down -v --remove-orphans >/dev/null 2>&1 || true; }
cleanup
[ "${KEEP:-0}" = 1 ] || trap cleanup EXIT
docker compose -p "$PROJECT" up -d --build
for i in $(seq 1 90); do
  if curl -fs "http://localhost:$APP_PORT/api/health" >/dev/null; then break; fi
  if [ "$i" -eq 90 ]; then docker compose -p "$PROJECT" logs app | tail -80; exit 1; fi
  sleep 2
done
node postman/build.mjs
npx -y newman@6.2.2 run postman/HotelBooking.postman_collection.json --env-var "baseUrl=http://localhost:$APP_PORT" "$@"
