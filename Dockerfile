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
    && useradd --system --uid 10001 --create-home osm

COPY --from=build --chown=osm:osm /workspace/app/target/osm-monolith.jar /app/osm-monolith.jar

ENV SERVER_PORT=8084 \
    JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:+UseSerialGC -Xms64m -Xmx256m -Xss512k -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=32m -XX:+ExitOnOutOfMemoryError" \
    JAVA_OPTS=""

USER osm

EXPOSE 8084

HEALTHCHECK --interval=15s --timeout=5s --start-period=120s --retries=8 \
    CMD sh -c 'curl --fail --silent "http://127.0.0.1:${PORT:-8084}/actuator/health/liveness" > /dev/null || exit 1'

ENTRYPOINT ["sh", "-c", "if [ -n \"$DATABASE_URL\" ]; then export DB_URL=\"$(printf '%s' \"$DATABASE_URL\" | sed -e 's#^postgres://#jdbc:postgresql://#' -e 's#^postgresql://#jdbc:postgresql://#')\"; fi; export DB_USER=\"${DB_USER:-${PGUSER:-postgres}}\"; export DB_PASS=\"${DB_PASS:-${PGPASSWORD:-}}\"; java $JAVA_OPTS -jar /app/osm-monolith.jar"]
