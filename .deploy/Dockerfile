# syntax = docker/dockerfile:1.2
#构建jar
FROM registry.cn-shenzhen.aliyuncs.com/rxxy/maven:3.6.0-jdk-8-alpine as build-jar
ARG JAR_NAME="miya-system.jar"
WORKDIR /app
COPY ./ /app/
COPY .deploy/settings.xml /usr/share/maven/ref/settings-docker.xml

RUN --mount=type=cache,mode=0777,target=/root/.m2/repository/,id=maven_cache \
        mvn -s /app/.deploy/settings.xml clean package -Dmaven.test.skip=true

#导出构件
FROM scratch AS export
COPY --from=build-jar /app/miya-system/target/${JAR_NAME} /

#打包为镜像
FROM registry.cn-shenzhen.aliyuncs.com/rxxy/java:8-jdk as build-image
COPY --from=export /${JAR_NAME} /${JAR_NAME}
ARG PROFILE=dev
ENV TZ Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE "${PROFILE}"
#ENV JAVA_OPS "-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/heapdump.hprof"
VOLUME /upload
EXPOSE 8080
ENTRYPOINT ["bash","-c", "java -jar miya-system.jar"]

#ENTRYPOINT ["bash","-c", "java -Dspring.profiles.active=${PROFILE} -agentlib:jdwp=transport=dt_socket,server=y,address=8000 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/heapdump.hprof -Dupload.absolutePath=/upload/ -jar /${NAME}.jar"]

#免打包
#FROM openjdk:8-jre
#COPY ./target/crm9-release/crm9 /crm9
#WORKDIR /
#EXPOSE 80
#CMD java -Xverify:none -cp /crm9/config:/crm9/lib/* com.kakarote.crm9.Application
