# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy parent POM and all module POMs so Maven can resolve the reactor graph
COPY pom.xml .
COPY patient-service/pom.xml patient-service/pom.xml
COPY appointment-service/pom.xml appointment-service/pom.xml

# Pre-download dependencies before copying source (layer caching)
RUN mvn dependency:go-offline -pl appointment-service -q

# Copy appointment-service source and build the JAR
COPY appointment-service/src appointment-service/src
RUN mvn -pl appointment-service package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for container security best practice
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/appointment-service/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
