# 基础镜像：使用 Java 17
FROM amazoncorretto:17-alpine

# 设置工作目录
WORKDIR /app

# 复制 Jar 包 (确保你先 mvn package 过)
COPY target/caregiver-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx512m", "app.jar"]