# Getting Started

**Documentation Author:** Bykov Lev  
**Last Updated:** December 9, 2025  
**Version:** 1.0.0

## Overview

This document explains how to run the LMS backend locally and with Docker. It also shows how to build and test single services.

## Prerequisites

Before you start, install:

- JDK 21 (for example, Temurin or Corretto)
- Docker and Docker Compose
- Git
- A terminal (Linux, macOS, or WSL on Windows)
- IntelliJ IDEA (recommended for development)

You do not need to install Gradle globally. The project uses the Gradle Wrapper (`./gradlew`).

## Project structure

The backend is a multi-module Gradle project.

Main parts:

- `services/` — microservices (authservice, userservice, courseService, and others)
- `services/common-security` — shared security module (JWT, filters, etc.)
- `shared/` — common classes and utilities

Each service has its own `README.md` with more details.

## Clone the project

```bash
git clone <repo-url>
cd LMS
```

Replace `<repo-url>` with the real repository URL.

## Run all services with Docker Compose

This option is good if you want to run the whole backend at once.

1. Make sure Docker and Docker Compose are running.
2. From the project root, run:

```bash
docker compose --profile all up --build
```

3. Wait until:

- PostgreSQL is up
- MongoDB is up
- Redis is up
- All microservices are started (authservice, userservice, courseService, etc.)
- API Gateway is started

Ports and more details are described in `docker-compose.yml` and in service README files.

To stop all containers:

```bash
docker compose --profile all down
```

## Run a single service locally (without Docker)

This option is good for development and debugging.

### Using Gradle from terminal

From the project root:

- Build and run tests for one service:

```bash
./gradlew :services:authservice:clean :services:authservice:build
./gradlew :services:userservice:clean :services:userservice:build
```

- Run only tests:

```bash
./gradlew :services:authservice:test
./gradlew :services:userservice:test
```

Many services support Spring profiles like `dev`, `docker`, and `test`.  
You can set a profile with an environment variable:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew :services:authservice:bootRun
```

### Using IntelliJ IDEA

1. Open the project as a Gradle project.
2. Set the Project SDK to JDK 21.
3. Wait until Gradle import is finished.
4. Open the main class of the service, for example:
    - `AuthserviceApplication` for authservice
    - `UserserviceApplication` for userservice
5. Create a Spring Boot run configuration:
    - Main class: `<Service>Application`
    - Active profiles: for example `dev` or `docker`
6. If the service needs external databases, set environment variables in the run configuration, for example:
    - `SPRING_DATASOURCE_URL`
    - `SPRING_DATASOURCE_USERNAME`
    - `SPRING_DATASOURCE_PASSWORD`
    - `SPRING_DATA_MONGODB_URI`

Check each service `README.md` to see which variables are required.

## Tests and H2 profile

For many tests, the project uses an in-memory H2 database and Spring profile `test`.

You can run tests with this profile from the terminal:

```bash
SPRING_PROFILES_ACTIVE=test ./gradlew :services:authservice:test
SPRING_PROFILES_ACTIVE=test ./gradlew :services:userservice:test
```

Each service has a `src/test/resources/application-test.yml` file that configures H2 and other test settings.
