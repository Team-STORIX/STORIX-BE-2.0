# Build Stage
FROM amazoncorretto:17-alpine-jdk AS builder

ENV TZ=Asia/Seoul
WORKDIR /app

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./
COPY src/main/resources ./src/main/resources

COPY STORIX-Common/build.gradle ./STORIX-Common/build.gradle
COPY STORIX-Domain/build.gradle ./STORIX-Domain/build.gradle
COPY STORIX-Infrastructure/build.gradle ./STORIX-Infrastructure/build.gradle
COPY STORIX-Batch/build.gradle ./STORIX-Batch/build.gradle

RUN ./gradlew :STORIX-Batch:dependencies --no-daemon

COPY STORIX-Common/src ./STORIX-Common/src
COPY STORIX-Domain/src ./STORIX-Domain/src
COPY STORIX-Infrastructure/src ./STORIX-Infrastructure/src
COPY STORIX-Batch/src ./STORIX-Batch/src

RUN ./gradlew :STORIX-Batch:bootJar -x test --no-daemon

# Run Stage
FROM amazoncorretto:17-alpine-jdk

ENV TZ=Asia/Seoul
WORKDIR /app

COPY --from=builder /app/STORIX-Batch/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
