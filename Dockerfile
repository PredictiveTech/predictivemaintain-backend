# ---------- Stage 1: build the jar ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Dependencies first: Docker reuses this layer while pom.xml does not change,
# so a code-only change rebuilds in seconds instead of downloading everything again.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src/ src/
# Tests run in CI; the image build only packages (the integration tests need Docker, which is not inside a build).
RUN ./mvnw -B package -DskipTests

# ---------- Stage 2: the small image that actually runs ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Never run as root: if the application is ever compromised, the attacker gets a user with no privileges.
RUN groupadd --system app && useradd --system --gid app --no-create-home app \
 && mkdir -p /data/evidence && chown -R app:app /data

COPY --from=build /workspace/target/*.jar /app/app.jar

USER app
EXPOSE 8080

# Use at most 75% of the memory the container has, and the lightest garbage collector (small instances).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]