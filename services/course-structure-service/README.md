# Course Structure Service

Микросервис для управления структурой курсов (модули и уроки).

## Требования

- **Java 21** или выше
- Gradle 8.x (включён в проект через wrapper)

## Установка Java 21 на Ubuntu/WSL2

### Способ 1: Через SDKMAN (Рекомендуется)

```bash
# Установить SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Установить Java 21 (Amazon Corretto)
sdk install java 21.0.5-amzn

# Или установить Temurin (Eclipse Foundation)
sdk install java 21.0.5-tem

# Проверить установку
java -version
```

### Способ 2: Через apt (Ubuntu 22.04+)

```bash
# Обновить список пакетов
sudo apt update

# Установить OpenJDK 21
sudo apt install openjdk-21-jdk -y

# Настроить Java 21 как версию по умолчанию
sudo update-alternatives --config java

# Проверить установку
java -version
javac -version
```

### Способ 3: Через apt с PPA (для старых версий Ubuntu)

```bash
# Добавить PPA репозиторий
sudo add-apt-repository ppa:openjdk-r/ppa
sudo apt update

# Установить OpenJDK 21
sudo apt install openjdk-21-jdk -y

# Установить переменные окружения
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# Проверить установку
java -version
```

### Проверка установки Java

После установки выполните:
```bash
java -version
```

Должно вывести что-то вроде:
```
openjdk version "21.0.5" 2024-10-15
OpenJDK Runtime Environment (build 21.0.5+11-Ubuntu-1ubuntu1)
OpenJDK 64-Bit Server VM (build 21.0.5+11-Ubuntu-1ubuntu1, mixed mode, sharing)
```

## Запуск тестов

### На Ubuntu/Linux/macOS

```bash
# Перейти в корневую директорию проекта
cd /path/to/LMS-main

# Запустить все тесты
./gradlew :services:course-structure-service:test

# Запустить тесты с принудительным перезапуском
./gradlew :services:course-structure-service:test --rerun-tasks

# Запустить только unit-тесты (сервисы)
./gradlew :services:course-structure-service:test --tests "ru.lms_project.coursestructure.service.*"

# Запустить только интеграционные тесты (контроллеры)
./gradlew :services:course-structure-service:test --tests "ru.lms_project.coursestructure.controller.*"

# Запустить конкретный тестовый класс
./gradlew :services:course-structure-service:test --tests "ru.lms_project.coursestructure.service.LessonServiceTest"

# Запустить конкретный тестовый метод
./gradlew :services:course-structure-service:test --tests "ru.lms_project.coursestructure.service.LessonServiceTest.createLesson_ShouldCreateLesson"
```

### На Windows (PowerShell/CMD)

```powershell
# Перейти в корневую директорию проекта
cd C:\path\to\LMS-main

# Запустить все тесты
.\gradlew :services:course-structure-service:test

# Запустить тесты с принудительным перезапуском
.\gradlew :services:course-structure-service:test --rerun-tasks

# Запустить только unit-тесты (сервисы)
.\gradlew :services:course-structure-service:test --tests "ru.lms_project.coursestructure.service.*"

# Запустить только интеграционные тесты (контроллеры)
.\gradlew :services:course-structure-service:test --tests "ru.lms_project.coursestructure.controller.*"
```

## Просмотр результатов тестов

После выполнения тестов, отчёт будет доступен по адресу:
```
services/course-structure-service/build/reports/tests/test/index.html
```

Откройте этот файл в браузере для просмотра детальных результатов.

## Тестовая конфигурация

Тесты используют **embedded MongoDB** (встроенная база данных в памяти), поэтому не требуется запускать отдельный экземпляр MongoDB для тестирования.

Конфигурация находится в:
- `src/test/resources/application-test.properties`
- `src/main/resources/application-test.properties`

## Структура тестов

```
src/test/java/
└── ru/lms_project/coursestructure/
    ├── controller/          # Интеграционные тесты контроллеров
    │   ├── LessonControllerIntegrationTest.java
    │   └── ModuleControllerIntegrationTest.java
    └── service/            # Unit-тесты сервисов
        ├── LessonServiceTest.java
        └── ModuleServiceTest.java
```

## Устранение проблем

### Ошибка "embedded MongoDB version not set"

Если возникает ошибка:
```
Set the de.flapdoodle.mongodb.embedded.version property
```

Убедитесь, что в `application-test.properties` указана версия MongoDB:
```properties
de.flapdoodle.mongodb.embedded.version=7.0.14
```

### Очистка кэша Gradle

```bash
# На Ubuntu/Linux/macOS
./gradlew clean

# На Windows
.\gradlew clean
```

## Continuous Integration

Для CI/CD рекомендуется использовать:
```bash
./gradlew :services:course-structure-service:test --rerun-tasks --no-daemon
```

Опция `--no-daemon` отключает Gradle daemon, что полезно в CI-окружении.
