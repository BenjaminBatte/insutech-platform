# ==========================
# Build stage
# ==========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the jar
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================
# Runtime stage
# ==========================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the jar from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the policy-service port
EXPOSE 9090

# Run the service
ENTRYPOINT ["java", "-jar", "app.jar"]
