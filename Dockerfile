# Use an official Maven image with OpenJDK 17
FROM maven:3.8.6-openjdk-18 AS build
# Set the working directory in the container
WORKDIR /app
COPY . /app
# Run Maven build

FROM openjdk:18-jdk-bullseye
WORKDIR /app
ARG JAR_FILE=target/news-scanner-0.0.1-SNAPSHOT.jar

RUN apt-get update
RUN apt-get install -y wget gnupg
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-keyring.gpg
RUN echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-keyring.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list
RUN apt-get update
RUN apt-get install -y google-chrome-stable
RUN google-chrome --version


COPY --from=build /app/target/news-scanner-0.0.1-SNAPSHOT.jar .
COPY --from=build /app/chromedriver  .
CMD ["java", "-jar", "news-scanner-0.0.1-SNAPSHOT.jar"]
