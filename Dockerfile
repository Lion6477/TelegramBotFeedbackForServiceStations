# ---------- STAGE 1: Build ----------
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# ---------- STAGE 2: Runtime ----------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /workspace/target/*SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
