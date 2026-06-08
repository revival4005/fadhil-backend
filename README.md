# FK Collection Backend

Spring Boot API for the FK Collection clothing store.

## Run locally

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Local API base URL:

```text
http://localhost:8082/api
```

Docker/server API base URL:

```text
http://5.189.190.132:8089/api
```

Spring Boot uses application properties:

```text
src/main/resources/application-dev.properties
src/main/resources/application-uat.properties
```

The `.env` file keeps the same simple style as the frontend:

```env
# local
API_BASE_URL="http://localhost:8082/api"
PORT=8082
SPRING_PROFILES_ACTIVE=dev
CORS_ORIGIN=http://localhost:3000,http://localhost:3002

# uat
#API_BASE_URL="http://5.189.190.132:8089/api"
#SPRING_PROFILES_ACTIVE=uat
#CORS_ORIGIN=http://5.189.190.132:3002
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
