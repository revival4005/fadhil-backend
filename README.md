# FK Collection Backend

Simple Express API for the FK Collection clothing store.

## Run locally

```bash
npm install
npm run dev
```

Local API base URL:

```text
http://localhost:8082/api
```

Docker/server API base URL:

```text
http://5.189.190.132:8089/api
```

## Main endpoints

```text
GET  /api/health
GET  /api/products
POST /api/auth/register
POST /api/auth/login
POST /api/orders
GET  /api/orders
POST /api/contact
GET  /api/contact
```

## Docker

```bash
docker compose -f compose.yml build
docker compose -f compose.yml up -d
```
