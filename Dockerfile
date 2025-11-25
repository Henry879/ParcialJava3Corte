# Etapa 1: Construir el proyecto con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .

RUN mvn -q -e -DskipTests clean package


# Etapa 2: Run con distroless (más seguro)
FROM gcr.io/distroless/java21

WORKDIR /
COPY --from=builder /app/target/saberpro-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8091

CMD ["app.jar"]