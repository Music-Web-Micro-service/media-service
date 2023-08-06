FROM openjdk:17
WORKDIR /app

COPY ./target/media-service-0.0.1-SNAPSHOT.jar ./target/media-service-0.0.1-SNAPSHOT.jar

EXPOSE 8088

CMD ["java", "-jar", "./target/media-service-0.0.1-SNAPSHOT.jar"]