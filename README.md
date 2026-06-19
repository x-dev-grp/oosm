# OSM 2.0 Modular Monolith

> **Documentation:** See the repo root [docs/](../docs/README.md) for architecture, getting started, and domain guides.

This project is the migration target for the OSM backend.

## Current Slice

- Java 21
- Spring Boot 3.4.4
- Maven multi-module build
- One deployable backend jar from `app`
- One PostgreSQL database named `osm`
- Flyway schema baseline
- Security migrated first from `osm-sec`
- Company profile remains inside `security` until security parity is accepted

## Module Rules

- `app` owns application bootstrap and runtime configuration.
- Business modules are Maven `jar` modules.
- Modules do not define independent Spring Boot applications.
- Modules do not define independent database connections.
- External module-to-module HTTP/Feign calls are replaced by internal ports in later slices.

## First Boot

Start PostgreSQL:

```powershell
docker compose up -d postgres
```

Build:

```powershell
mvn clean package -DskipTests
```

Run:

```powershell
mvn -pl app spring-boot:run
```

The app listens on port `8084` to preserve current frontend gateway compatibility during migration.

## Database Mode

The first security slice uses:

```yaml
spring.jpa.hibernate.ddl-auto: ${HIBERNATE_DDL_AUTO:update}
```

After security tables are verified, export the generated security schema into Flyway migrations and switch to:

```yaml
spring.jpa.hibernate.ddl-auto: validate
```
