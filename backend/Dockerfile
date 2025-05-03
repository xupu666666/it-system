FROM maven:3.8.5-openjdk-17 AS build

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件
COPY pom.xml .

# 下载依赖（这一步会被缓存，除非pom.xml有更改）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src/ /app/src/

# 构建应用
RUN mvn package -DskipTests

# 运行阶段使用更小的基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制构建好的jar文件
COPY --from=build /app/target/*.jar app.jar

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod

# 暴露端口
EXPOSE 8090

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"] 