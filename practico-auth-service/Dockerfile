FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml pom.xml
COPY practico-auth-contract/pom.xml practico-auth-contract/pom.xml
COPY practico-auth-service/pom.xml practico-auth-service/pom.xml
COPY practico-auth-contract/src practico-auth-contract/src
COPY practico-auth-service/src practico-auth-service/src

RUN mvn -pl practico-auth-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/practico-auth-service/target/*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
