FROM eclipse-temurin:17-jre

ENV TZ=Asia/Seoul
WORKDIR /app

COPY STORIX-Batch/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
