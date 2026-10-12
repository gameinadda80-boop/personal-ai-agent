# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the JAR, skipping tests to speed up the process
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the JAR from the build stage
COPY --from=build /app/target/personal-ai-agent-1.0-SNAPSHOT.jar app.jar
# Expose port 8080
EXPOSE 8080
# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]