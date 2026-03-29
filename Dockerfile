FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY target/scheduler-1.0.0.jar app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
