# syntax = docker/dockerfile:1.2

FROM registry.cn-hangzhou.aliyuncs.com/rxxy/java:8-jdk
WORKDIR /app
COPY ./target/miya-system.jar /app/miya-system.jar

ARG PROFILE=common,prod
ENV TZ Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE "${PROFILE}"
EXPOSE 8080
ENTRYPOINT ["bash","-c", "java -jar miya-system.jar"]

