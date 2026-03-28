# 多阶段构建 Dockerfile - 使用已拉取成功的镜像
FROM maven:3.8.8-eclipse-temurin-8 AS backend-build

# 构建后端
WORKDIR /app/backend
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM node:18-alpine AS frontend-build

# 构建前端
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm install --only=production

COPY frontend/public ./public
COPY frontend/src ./src

# 允许在构建时传入服务器IP地址
ARG REACT_APP_SERVER_IP
ENV REACT_APP_SERVER_IP=${REACT_APP_SERVER_IP}

RUN npm run build

# 生产阶段 - 使用 nginx:alpine（如果拉取失败可换为 nginx:stable-alpine）
FROM nginx:stable-alpine

# 安装 Java 运行时
RUN apk add --no-cache openjdk8-jre

# 配置 nginx
COPY nginx.conf /etc/nginx/nginx.conf

# 复制后端 jar 文件
COPY --from=backend-build /app/backend/target/*.jar /app/backend.jar

# 复制前端构建文件
COPY --from=frontend-build /app/frontend/build /usr/share/nginx/html

# 创建启动脚本
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'nginx &' >> /app/start.sh && \
    echo 'java -jar /app/backend.jar' >> /app/start.sh && \
    chmod +x /app/start.sh

EXPOSE 80

CMD ["/app/start.sh"]