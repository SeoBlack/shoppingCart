# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .

RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# Stage 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built jar from previous stage
COPY --from=build /app/target/*.jar app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]