FROM openjdk:17-jdk
COPY build/libs/*SNAPSHOT.jar app.jar
COPY src/main/resources/application-deploy.yaml application-deploy.yaml
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=deploy", "/app.jar"]