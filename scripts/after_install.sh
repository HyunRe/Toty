#!/bin/bash

echo "=== AfterInstall: Setting up configuration ==="

cd /home/ubuntu/toty-app

# .env 파일이 없으면 에러
if [ ! -f .env ]; then
    echo "ERROR: .env file not found!"
    echo "Please create .env file in /home/ubuntu/toty-app/"
    exit 1
fi

# Firebase 설정 파일 확인
if [ ! -d config/firebase ] || [ -z "$(ls -A config/firebase)" ]; then
    echo "WARNING: Firebase configuration not found in config/firebase/"
    echo "Please upload Firebase credentials to config/firebase/"
fi

# Docker 이미지 정리 (디스크 공간 확보)
echo "Cleaning up old Docker images..."
sudo docker image prune -af --filter "until=24h"

# 권한 설정
sudo chown -R ubuntu:ubuntu /home/ubuntu/toty-app

echo "Configuration setup completed"
exit 0
