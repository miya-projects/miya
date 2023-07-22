#syntax = docker/dockerfile:1.2

#打包为镜像
FROM registry.cn-hangzhou.aliyuncs.com/rxxy/java:17-jdk as build-image
WORKDIR /app
COPY target/*.jar /app/
ARG PROFILE=dev
ENV TZ Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE "${PROFILE}"
ENV JAVA_OPS "-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/heapdump.hprof"
#VOLUME /upload
#EXPOSE 8080
CMD ["bash","-c", "java -jar miya-examples.jar"]
#ENTRYPOINT ["bash","-c", "java -jar miya-system.jar"]
