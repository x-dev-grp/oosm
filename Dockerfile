FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY modules ./modules
COPY app ./app

RUN mvn -B -DskipTests package -pl app -am

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home oosm

COPY --from=build --chown=oosm:oosm /workspace/app/target/oosm-monolith.jar /app/oosm-monolith.jar
COPY --chown=oosm:oosm docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENV SERVER_PORT=8084 \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:+UseSerialGC -Xms64m -Xmx256m -Xss512k -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=32m -XX:+ExitOnOutOfMemoryError" \
    JAVA_OPTS=""

USER oosm

EXPOSE 8084

HEALTHCHECK --interval=15s --timeout=5s --start-period=120s --retries=8 \
    CMD sh -c 'curl --fail --silent "http://127.0.0.1:${PORT:-8084}/actuator/health/liveness" > /dev/null || exit 1'

ENTRYPOINT ["/app/entrypoint.sh"]
