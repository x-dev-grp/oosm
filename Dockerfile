# syntax=docker/dockerfile:1.7

FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY modules ./modules
COPY app ./app

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package -pl app -am

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home osm

COPY --from=build --chown=osm:osm /workspace/app/target/osm-monolith.jar /app/osm-monolith.jar

ENV SERVER_PORT=8084 \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError" \
    JAVA_OPTS=""

USER osm

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD curl --fail --silent "http://localhost:${PORT:-${SERVER_PORT}}/actuator/health/liveness" > /dev/null || exit 1

ENTRYPOINT ["sh", "-c", "if [ -n \"$DATABASE_URL\" ] && [ -z \"$DB_URL\" ]; then export DB_URL=\"$(printf '%s' \"$DATABASE_URL\" | sed -e 's#^postgres://#jdbc:postgresql://#' -e 's#^postgresql://#jdbc:postgresql://#')\"; fi; java $JAVA_OPTS -jar /app/osm-monolith.jar"]
