#syntax = docker/dockerfile:1.2

FROM registry.cn-hangzhou.aliyuncs.com/rxxy/java:17-jdk
WORKDIR /app
COPY ./target/miya-system.jar /app/miya-system.jar

#RUN --mount=type=cache,mode=0777,target=~/.m2/repository/,id=maven_cache \
#        mvn -s /root/.m2/settings.xml -P fatjar clean package -Dmaven.test.skip=true


