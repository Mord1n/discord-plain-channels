FROM maven:3.9.6-eclipse-temurin-17-focal

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# update packages
RUN apt update
RUN mkdir /usr/src/app

COPY target/*.jar /usr/src/app/discord_bot.jar

WORKDIR /usr/src/app
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "discord_bot.jar"]