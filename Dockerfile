# =========================
# Build stage
# =========================
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# =========================
# Run stage
# =========================
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy jar file from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
