FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /project

COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -e -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /work

COPY --from=build /project/target/*-runner.jar /work/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/work/app.jar"]
