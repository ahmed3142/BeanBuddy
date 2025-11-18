# Stage 1: Build the application with Maven
# Use a Maven image that includes Java 17
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml (from source 1) first to leverage Docker layer caching
COPY pom.xml .

# Copy the rest of the source code
COPY src ./src

# Package the application, skipping tests. This will create the .jar file.
RUN mvn package -DskipTests

# Stage 2: Create the final runtime image
# Use the slim Java 17 runtime (as you did in source 2)
FROM openjdk:17-slim

# Set a working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage
# Your pom.xml (source 1) defines the artifactId as "BeanBuddies" and version as "0.0.1-SNAPSHOT"
COPY --from=build /app/target/BeanBuddies-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on (from source 2)
EXPOSE 8081

# Run the application (from source 2)
ENTRYPOINT ["java", "-jar", "/app.jar"]