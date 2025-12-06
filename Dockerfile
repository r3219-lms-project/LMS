FROM eclipse-temurin:21-jdk-alpine AS builder

ARG SERVICE_NAME
WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

COPY shared shared

COPY services services

RUN chmod +x ./gradlew

RUN ./gradlew :services:${SERVICE_NAME}:build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runner

ARG SERVICE_NAME
ARG SERVICE_PORT
WORKDIR /app

COPY --from=builder /workspace/app/services/${SERVICE_NAME}/build/libs/*.jar app.jar

EXPOSE ${SERVICE_PORT}

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
