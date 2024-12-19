# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
# Tải dependencies trước
RUN mvn dependency:go-offline

# Copy source và build
COPY src ./src
COPY .env .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/.env .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]