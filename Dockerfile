# syntax=docker/dockerfile:1

# ---- Etapa de build ----
FROM maven:3.9.15-eclipse-temurin-26 AS build
WORKDIR /build
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN mvn -B -ntp dependency:go-offline
COPY src src
RUN mvn -B -ntp -DskipTests package

# ---- Etapa de ejecucion ----
FROM eclipse-temurin:17.0.13_11-jre-alpine AS runtime

RUN addgroup -S facturador && adduser -S facturador -G facturador
WORKDIR /app
COPY --from=build /build/target/facturador-sri-backend-*.jar app.jar
RUN chown -R facturador:facturador /app
USER facturador

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
