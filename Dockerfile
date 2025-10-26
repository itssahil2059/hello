FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY . .
RUN mvn -q -B -DskipTests package

# Use non-alpine runtime (multi-arch, works on Apple Silicon)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/target/*-with-deps.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]