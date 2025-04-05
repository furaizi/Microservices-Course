FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon
COPY deps ./deps
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system appuser && useradd -m --system --gid appuser appuser
COPY --from=builder --chown=appuser:appuser /app/build/libs/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
