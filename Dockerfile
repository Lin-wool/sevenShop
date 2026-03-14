FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml backend/
COPY backend/src backend/src
RUN mvn -f backend/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/backend/target/seven-shop-1.0.0.jar app.jar

# Railway 环境变量会在运行时自动注入
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
