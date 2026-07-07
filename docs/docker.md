# Docker (Sprint 9 / Section 5)

## Что добавлено

- `practico-core-service/Dockerfile`
- `practico-auth-service/Dockerfile`
- `docker/frontend/Dockerfile`
- `docker/frontend/nginx.conf`
- `docker/nginx/nginx.conf`
- `docker-compose.yml`

## Важно по frontend

В `docker-compose.yml` frontend собирается из контекста `../mastery-web`:

- `context: ../mastery-web`
- `dockerfile: ../practico/docker/frontend/Dockerfile`

Это сделано, потому что текущий репозиторий `practico` содержит backend-модули, а React frontend находится в соседнем репозитории.

## Reverse proxy (Sprint 9 / Section 6)

Nginx настроен как единая точка входа:

- HTTP `:80` -> redirect на HTTPS
- HTTPS `:443` -> reverse proxy:
  - `/api/auth/*` -> `auth:8081`
  - `/api/*` -> `core:8080`
  - `/` -> `frontend:80`

Также включены:

- `gzip`
- security headers (`HSTS`, `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy`, `CSP`)

Для HTTPS нужны сертификаты:

- `docker/nginx/certs/fullchain.pem`
- `docker/nginx/certs/privkey.pem`
