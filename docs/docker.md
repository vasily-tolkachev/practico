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
