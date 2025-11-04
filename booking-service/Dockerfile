FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY target/booking-service-0.0.1-SNAPSHOT.jar application.jar

RUN apt-get update && apt-get install -y curl

ENTRYPOINT ["java", "-jar", "application.jar"]
EXPOSE 8084
