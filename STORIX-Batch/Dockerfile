# Build Stage
FROM amazoncorretto:17-alpine-jdk AS builder

ENV TZ=Asia/Seoul
WORKDIR /app

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

COPY STORIX-Common/build.gradle ./STORIX-Common/build.gradle
COPY STORIX-Domain/build.gradle ./STORIX-Domain/build.gradle
COPY STORIX-Infrastructure/build.gradle ./STORIX-Infrastructure/build.gradle
COPY STORIX-Batch/build.gradle ./STORIX-Batch/build.gradle

COPY . .

RUN ./gradlew :STORIX-Batch:bootJar -x test --no-daemon

# Run Stage
FROM eclipse-temurin:17-jre-alpine

ENV TZ=Asia/Seoul
WORKDIR /app

COPY --from=builder /app/STORIX-Batch/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
