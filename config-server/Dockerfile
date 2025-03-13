FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/config-server-0.0.1-SNAPSHOT.jar application.jar

RUN apt-get update && apt-get install -y curl

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "application.jar"]


