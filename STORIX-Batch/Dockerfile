FROM eclipse-temurin:17-jre

ENV TZ=Asia/Seoul
WORKDIR /app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"

COPY STORIX-Batch/build/libs/*.jar app.jar

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Duser.timezone=Asia/Seoul -jar app.jar"]
