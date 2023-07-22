#syntax = docker/dockerfile:1.2
FROM registry.cn-hangzhou.aliyuncs.com/rxxy/maven:3.9.3-sapmachine-17 as build
WORKDIR /app
COPY ./ /app/
RUN --mount=type=cache,mode=0777,target=~/.m2/repository/,id=maven_cache \
        mvn -s /root/.m2/settings.xml -P fatjar clean package -Dmaven.test.skip=true

#导出构件
FROM scratch AS export
COPY --from=build /app/target/*.jar /

#打包为镜像
FROM registry.cn-hangzhou.aliyuncs.com/rxxy/java:17-jdk as build-image
WORKDIR /app
COPY --from=export /* /app/
ARG PROFILE=dev
ENV TZ Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE "${PROFILE}"
ENV JAVA_OPS "-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/heapdump.hprof"
#VOLUME /upload
#EXPOSE 8080
CMD ["bash","-c", "java -jar miya-examples.jar"]
#ENTRYPOINT ["bash","-c", "java -jar miya-system.jar"]
