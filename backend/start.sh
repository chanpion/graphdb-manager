#!/bin/bash

# 图数据库管理系统后端启动脚本

echo "启动图数据库管理系统后端服务..."

# 检查是否安装了Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未安装Maven，请先安装Maven"
    exit 1
fi

# 编译项目
echo "编译项目..."
cd /Users/chenpp/CodeBuddy/20260109115616/graphdb-manager/backend
mvn clean compile

if [ $? -ne 0 ]; then
    echo "编译失败，请检查错误信息"
    exit 1
fi

# 启动应用
echo "启动应用..."
cd /Users/chenpp/CodeBuddy/20260109115616/graphdb-manager/backend/graphdb-api
mvn spring-boot:run

echo "后端服务已启动，访问地址: http://localhost:8080"
echo "API文档地址: http://localhost:8080/swagger-ui.html"