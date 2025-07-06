FROM eclipse-temurin:21-jre-alpine

WORKDIR /work/
COPY target/*-runner.jar app.jar

CMD ["java", "-jar", "app.jar"]
