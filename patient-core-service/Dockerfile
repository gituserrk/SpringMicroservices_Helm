# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy parent POM and all module POMs so Maven can resolve the reactor graph.
# Both POMs are needed even though we only build patient-core-service — Maven reads
# all declared <modules> from the parent before applying -pl filtering.
COPY pom.xml .
COPY patient-core-service/pom.xml patient-core-service/pom.xml
COPY patient-appointment-service/pom.xml patient-appointment-service/pom.xml

# Pre-download dependencies before copying source.
# This layer is cached and reused on rebuilds as long as no POM changes.
RUN mvn dependency:go-offline -pl patient-core-service -q

# Copy patient-core-service source and build the JAR
COPY patient-core-service/src patient-core-service/src
RUN mvn -pl patient-core-service package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for container security best practice
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/patient-core-service/target/*.jar app.jar

EXPOSE 8080

# Kubernetes liveness/readiness probes hit /actuator/health — validate it here too
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Cap JVM heap at 75% of the container's cgroup memory limit
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
