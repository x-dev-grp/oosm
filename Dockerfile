FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY modules ./modules
COPY app ./app
COPY VERSION .

ARG GIT_SHA=unknown
RUN VERSION=$(tr -d '\r\n' < VERSION) && \
    printf 'app.version=%s\napp.git.sha=%s\napp.built.at=%s\n' \
      "$VERSION" "${GIT_SHA}" "$(date -u +%Y-%m-%dT%H:%M:%SZ)" > app/src/main/resources/version.properties && \
    mvn -B -DskipTests package -pl app -am

FROM eclipse-temurin:21-jre-jammy AS runtime

ARG APP_VERSION=unknown
LABEL org.opencontainers.image.version="${APP_VERSION}"

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home oosm

COPY --from=build --chown=oosm:oosm /workspace/app/target/oosm-monolith.jar /app/oosm-monolith.jar
COPY --chown=oosm:oosm docker/entrypoint.sh /app/entrypoint.sh
RUN sed -i 's/\r$//' /app/entrypoint.sh && chmod +x /app/entrypoint.sh

ENV JAVA_OPTS=""

USER oosm

EXPOSE 8084

HEALTHCHECK --interval=15s --timeout=5s --start-period=300s --retries=10 \
    CMD sh -c 'curl --fail --silent "http://127.0.0.1:${PORT:-8084}/actuator/health/liveness" > /dev/null || exit 1'

ENTRYPOINT ["/app/entrypoint.sh"]
