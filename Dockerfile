FROM openjdk:17 as builder
WORKDIR /app
COPY . .

#permission
RUN chmod +x mvnw
RUN ./mvnw clean package

# Use an official OpenJDK image with JDK as a base image
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot application JAR file into the container
COPY --from=builder target/imageMedia-service-0.0.1-SNAPSHOT.jar /app/

# Expose the port that your Eureka server listens on
EXPOSE 8088

# Command to run your Spring Boot application
CMD ["java", "-jar", "imageMedia-service-0.0.1-SNAPSHOT.jar"]