# OhMyUniversity! - Fetcher

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=flat&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/license-AGPL%203.0-blue?style=flat)

Fetcher microservice for the **OhMyUniversity!** platform - part of the [OhMyOpenSource!](https://github.com/ohmyopensource) organization.

---

## What is this?

This microservice is responsible for syncing external data into the OhMyUniversity! platform through scheduled jobs. It periodically fetches data from Italian and European public sources - including MUR, InPA, EPSO and professional registers - and persists it locally so that other microservices can serve it to students without depending on external API availability or rate limits.

Built with Spring Batch on top of Spring Boot 4.

---

## Part of OhMyUniversity!

OhMyUniversity! is an open source university platform designed to simplify academic life for students, professors, and administrative staff. It provides real-time chat, institutional data integration, course and canteen management, transport schedules, room booking, and more.

### Public repositories

| Repository | Description |
|---|---|
| [ohmyuniversity-gateway](https://github.com/ohmyopensource/ohmyuniversity-gateway) | API Gateway |
| [ohmyuniversity-core](https://github.com/ohmyopensource/ohmyuniversity-core) | Core API - institutional data, courses, canteen, transport |
| [ohmyuniversity-chat](https://github.com/ohmyopensource/ohmyuniversity-chat) | Chat microservice - real-time WebSocket messaging |
| [ohmyuniversity-fetcher](https://github.com/ohmyopensource/ohmyuniversity-fetcher) | This repo - Fetcher |
| [ohmyuniversity-web](https://github.com/ohmyopensource/ohmyuniversity-web) | Web frontend - Angular |
| [ohmyuniversity-mobile](https://github.com/ohmyopensource/ohmyuniversity-mobile) | Mobile app - Flutter |
| [ohmyuniversity-desktop](https://github.com/ohmyopensource/ohmyuniversity-desktop) | Desktop app - Tauri |

---

## Documentation & guidelines

- **Full platform documentation:** [ohmyuniversity-docs](https://github.com/ohmyopensource/ohmyuniversity-docs)
- **Organization guidelines:** [ohmyopensource-guidelines](https://github.com/ohmyopensource/ohmyopensource-guidelines)

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Job scheduling | Spring Batch |
| HTTP client | Spring WebFlux WebClient (reactive) |
| Persistence | Spring Data JPA + PostgreSQL |
| DB migrations | Flyway |
| Messaging | Apache Kafka (producer) |
| Metrics | Micrometer + Prometheus |
| Tracing | Micrometer Tracing + OpenTelemetry |
| Mapping | MapStruct |
| Code style | Google Java Style Guide (Checkstyle) |

---

## Getting started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker

### Run locally

This repository includes a `Dockerfile`. To run the full platform locally (including all required infrastructure), refer to the infrastructure setup described in the platform documentation:

📚 [ohmyuniversity-docs](https://github.com/ohmyopensource/ohmyuniversity-docs)

### Build

```bash
./mvnw clean install
```

Checkstyle runs automatically during the `validate` phase. The build fails if the code does not comply with Google Java Style Guidelines.

### Build Docker image

```bash
docker build -t ohmyuniversity-fetcher .
```

---

## License

This project is licensed under the AGPL-3.0 - see the [LICENSE](LICENSE) file for details.