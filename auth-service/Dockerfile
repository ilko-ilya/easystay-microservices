FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY target/auth-service-0.0.1-SNAPSHOT.jar application.jar

RUN apt-get update && apt-get install -y curl

ENTRYPOINT ["java", "-jar", "application.jar"]
EXPOSE 8081
