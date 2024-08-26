FROM docker.io/library/maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install

#stage 2
FROM docker.io/library/openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/mediabot.jar ./media.jar
EXPOSE 8080
CMD ["java","-jar","media.jar"]