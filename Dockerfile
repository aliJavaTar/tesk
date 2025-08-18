# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app
COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy

ARG APP_USER=appuser
ARG APP_UID=1001
RUN groupadd --gid ${APP_UID} ${APP_USER} \
 && useradd --uid ${APP_UID} --gid ${APP_UID} --create-home --shell /bin/sh ${APP_USER}

WORKDIR /app

COPY --from=build --chown=${APP_USER}:${APP_USER} /app/target/reservation-0.0.1.jar /app/app.jar

RUN mkdir -p /app/logs && chown -R ${APP_USER}:${APP_USER} /app

USER ${APP_USER}

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
