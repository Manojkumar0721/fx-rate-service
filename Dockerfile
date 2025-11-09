# This file tells Render exactly how to build and run your Java application.

# --- Stage 1: Build the Application ---
# Use an official Java 17 (or newer) image to build the project
FROM eclipse-temurin:17-jdk-jammy AS build

# Set the working directory inside the container
WORKDIR /workspace/app

# Copy the Maven wrapper files
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Grant execute permission to the Maven wrapper (fixes "Permission denied")
RUN chmod +x ./mvnw

# Copy the source code
COPY src ./src

# Build the project and create the .jar file. Skip tests to speed up deployment.
RUN ./mvnw clean install -DskipTests

# --- Stage 2: Create the Final Runtime Image ---
# Use a lightweight image with only the Java Runtime (JRE) for efficiency
FROM eclipse-temurin:17-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built .jar file from the 'build' stage to this new image
# Make sure your .jar file name matches!
COPY --from=build /workspace/app/target/fx-rate-service-0.0.1-SNAPSHOT.jar app.jar

# Tell Render that your application will run on port 8080
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]