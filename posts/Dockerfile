FROM maven:3-jdk-11 AS build
WORKDIR /app
COPY ./ ./
RUN mvn clean install

FROM openjdk:11
EXPOSE 8080
WORKDIR /app
COPY --from=build /app/target/service.jar ./
CMD ["java","-jar","service.jar"]