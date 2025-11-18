# Stage 1: Build the application with Maven
# Use a Maven image that includes Java 17
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Copy the rest of the source code
COPY src ./src

# Package the application, skipping tests. This will create the .jar file.
RUN mvn package -DskipTests

# --- EIKHANE CHANGE KORA HOYECHE ---
# Stage 2: Create the final runtime image
# We'll use the Eclipse Temurin JRE, which is a modern OpenJDK build
FROM eclipse-temurin:17-jre-jammy
# --- END OF CHANGE ---

# Set a working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage
# Your pom.xml defines the artifactId as "BeanBuddies" and version as "0.0.1-SNAPSHOT"
COPY --from=build /app/target/BeanBuddies-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]