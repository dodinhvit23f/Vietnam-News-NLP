# Use an official Maven image with OpenJDK 17
FROM maven:3.8.1-openjdk-17
RUN mvn clean install
# Set working directory inside the container
WORKDIR /app

# Run Maven build


#FROM openjdk:8-jdk-alpine
#ARG JAR_FILE=target/news-scanner-0.0.1-SNAPSHOT.jar

#COPY chromedriver chromedriver
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]
