# Use OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Create and set working directory
WORKDIR /app

# Copy the jar file from target folder
COPY target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
