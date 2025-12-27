#!/bin/bash

echo "Starting containers with docker-compose..."
sudo docker-compose up -d --build

# 앱 초기화 시간을 고려해 35초 대기로 변경
echo "Waiting 30 seconds for Spring Boot initialization..."
sleep 35

sudo docker-compose ps