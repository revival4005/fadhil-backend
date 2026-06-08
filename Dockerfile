FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/fashion-backend-1.0.0.jar app.jar

ENV PORT=8082
ENV SPRING_PROFILES_ACTIVE=uat

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
