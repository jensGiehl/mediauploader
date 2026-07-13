# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -B -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S app && adduser -S -G app app \
    && mkdir -p /data/uploads \
    && chown -R app:app /app /data/uploads

COPY --from=build --chown=app:app /workspace/target/media-uploader-*.jar app.jar

USER app
EXPOSE 8080
VOLUME ["/data/uploads"]

ENV UPLOAD_DIRECTORY=/data/uploads
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
