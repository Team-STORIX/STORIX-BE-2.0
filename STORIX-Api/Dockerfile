FROM eclipse-temurin:17-jre-alpine

ENV TZ=Asia/Seoul
WORKDIR /app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"

COPY STORIX-Api/build/libs/*.jar app.jar

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Duser.timezone=Asia/Seoul -jar app.jar"]
