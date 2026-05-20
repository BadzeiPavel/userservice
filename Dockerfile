FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY target/userservice-0.0.1-SNAPSHOT.jar /app/userservice.jar

EXPOSE 8081

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "userservice.jar"]