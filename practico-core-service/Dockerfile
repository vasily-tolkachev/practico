FROM eclipse-temurin:25-jre
WORKDIR /app

COPY practico-core-service/target/*SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
