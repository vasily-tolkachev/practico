# Docker (Sprint 9 / Sections 5-6)

## Что добавлено

- `practico-core-service/Dockerfile`
- `practico-auth-service/Dockerfile`
- `docker/nginx/nginx.local.conf`
- `docker/nginx/nginx.prod.conf`
- `docker-compose.yml` (local)
- `docker-compose.prod.yml` (prod override)

## Локальный запуск (без HTTPS)

Локально используется только HTTP и `nginx.local.conf`.

```bash
docker compose up -d
```

## Production запуск (с HTTPS)

В production используется override-файл `docker-compose.prod.yml` и `nginx.prod.conf`.

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Для HTTPS нужны сертификаты:

- `docker/nginx/certs/fullchain.pem`
- `docker/nginx/certs/privkey.pem`

## Self-signed сертификат (временно)

Windows (PowerShell, нужен `openssl` в PATH):

```powershell
.\docker\nginx\certs\generate-self-signed.ps1 -Domain localhost -Days 365
```

Linux/macOS:

```bash
sh ./docker/nginx/certs/generate-self-signed.sh localhost 365
```
