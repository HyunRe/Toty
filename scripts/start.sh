#!/bin/bash

echo "=== ApplicationStart: Starting containers ==="

cd /home/ubuntu/toty-app

# .env 파일 로드
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Docker Compose로 컨테이너 시작
echo "Starting containers with docker-compose..."
sudo docker-compose up -d --build

# 컨테이너 시작 대기
echo "Waiting 35 seconds for Spring Boot initialization..."
sleep 35

# 컨테이너 상태 확인
sudo docker-compose ps

echo "Containers started successfully"
exit 0
