# Многоэтапная сборка
FROM maven:3.9-eclipse-temurin-17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы Maven для кэширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ для запуска
FROM eclipse-temurin:17-jre-jammy

# Обновляем пакеты и устанавливаем curl для health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Копируем jar файл из этапа сборки
COPY --from=build /app/target/*.jar app.jar

# Создаем директории и устанавливаем права
RUN mkdir -p logs uploads && chown -R appuser:appuser /app

# Переключаемся на пользователя
USER appuser

# Открываем порт
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Запускаем приложение с оптимизированными JVM параметрами
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-XX:+UseContainerSupport", "-jar", "app.jar"]
