FROM eclipse-temurin:25-jre
WORKDIR /app

COPY practico-auth-service/target/*SNAPSHOT.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
