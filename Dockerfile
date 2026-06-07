FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY modules ./modules
COPY app ./app

RUN mvn -B -DskipTests clean package -pl app -am

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/app/target/osm-monolith.jar /app/osm-monolith.jar

ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/osm-monolith.jar"]
