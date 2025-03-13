FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/payment-service-0.0.1-SNAPSHOT.jar application.jar

RUN apt-get update && apt-get install -y curl

ENTRYPOINT ["java", "-jar", "application.jar"]
EXPOSE 8085
