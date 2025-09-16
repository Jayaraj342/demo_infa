# docker run -p 8080:8080 demo_infa

# Use a lightweight Java 17 runtime image
FROM eclipse-temurin:17-jre

# Set working directory inside the container
WORKDIR /app

# Copy the jar file from target directory
COPY target/*.jar demo.jar

# Expose default Spring Boot port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "demo.jar"]
