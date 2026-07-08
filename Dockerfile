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
ARG NEW_RELIC_AGENT_VERSION=8.19.0
LABEL org.opencontainers.image.version="${APP_VERSION}"

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates postgresql-client \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home oosm \
    && mkdir -p /app/newrelic \
    && curl -fsSL "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NEW_RELIC_AGENT_VERSION}/newrelic-agent-${NEW_RELIC_AGENT_VERSION}.jar" \
        -o /app/newrelic/newrelic.jar \
    && chown -R oosm:oosm /app/newrelic

COPY --from=build --chown=oosm:oosm /workspace/app/target/oosm-monolith.jar /app/oosm-monolith.jar
COPY --from=build --chown=oosm:oosm /workspace/app/src/main/resources/newrelic.yml /app/newrelic/newrelic.yml
COPY --chown=oosm:oosm docker/entrypoint.sh /app/entrypoint.sh
RUN sed -i 's/\r$//' /app/entrypoint.sh && chmod +x /app/entrypoint.sh

ENV JAVA_OPTS=""

USER oosm

EXPOSE 8084

HEALTHCHECK --interval=15s --timeout=5s --start-period=300s --retries=10 \
    CMD sh -c 'curl --fail --silent "http://127.0.0.1:${PORT:-8084}/actuator/health/liveness" > /dev/null || exit 1'

ENTRYPOINT ["/app/entrypoint.sh"]
