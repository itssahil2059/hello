FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/*jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
