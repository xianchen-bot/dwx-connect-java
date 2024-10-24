#!/bin/bash
set -e
ROOT=$(
  cd $(dirname $0)/
  pwd
)
cd ${ROOT}

echo "${ROOT}"

APP_NAME="dwxconnect"

# 检查是否已经启动
pid=$(ps -ef | grep ${ROOT}/target/${APP_NAME} | grep -v grep | awk '{print $2}')
pid=${pid:=0}

if [ $pid -gt 0 ]; then
  echo "${APP_NAME} is already running"
  exit 1
fi

# 创建 logs 目录
mkdir -p logs

# 启动 Spring Boot 应用
nohup java -jar ${ROOT}/target/${APP_NAME}*.jar >logs/catalina.out 2>&1 &

sleep 10
./check.sh
tail -n 20 logs/dwx_connect_java.log
echo "Application Started"