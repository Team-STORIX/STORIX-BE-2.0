# 1. Build Stage
FROM amazoncorretto:17-alpine-jdk AS builder

ENV TZ=Asia/Seoul
WORKDIR /app

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

RUN ./gradlew dependencies

COPY . .

RUN ./gradlew build -x test --no-daemon


# 2. Run Stage
FROM amazoncorretto:17-alpine-jdk

ENV TZ=Asia/Seoul
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","app.jar"]