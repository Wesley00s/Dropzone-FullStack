FROM gradle:9.2.1-jdk21-jammy AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./

RUN gradle dependencies --no-daemon

COPY src ./src
RUN gradle bootJar --no-daemon -x test && \
    java -Djarmode=layertools -jar build/libs/*.jar extract --destination build/extracted

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=builder /app/build/extracted/dependencies/ ./
COPY --from=builder /app/build/extracted/spring-boot-loader/ ./
COPY --from=builder /app/build/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/build/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]