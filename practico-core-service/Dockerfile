FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml pom.xml
COPY practico-auth-contract/pom.xml practico-auth-contract/pom.xml
COPY practico-core-service/pom.xml practico-core-service/pom.xml
COPY practico-auth-contract/src practico-auth-contract/src
COPY practico-core-service/src practico-core-service/src

RUN mvn -pl practico-core-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/practico-core-service/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
