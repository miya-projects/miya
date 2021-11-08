# syntax = docker/dockerfile:1.2

FROM registry.cn-hangzhou.aliyuncs.com/rxxy/java:8-jdk
WORKDIR /app
COPY ./target/miya-examples.jar /app/miya-examples.jar

ARG PROFILE=prod
ENV TZ Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE "${PROFILE}"
EXPOSE 8080
ENTRYPOINT ["bash","-c", "java -jar miya-examples.jar"]

