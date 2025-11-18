# Step 1: Use an official Java 17 runtime (apnar project Java 17 use korchilo)
FROM openjdk:17-slim

# Step 2: Set an argument for the JAR file name
ARG JAR_FILE=target/*.jar

# Step 3: Copy the built .jar file from the 'target' folder into the container
COPY ${JAR_FILE} app.jar

# Step 4: Tell Render that your app runs on port 8081
EXPOSE 8081

# Step 5: Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]