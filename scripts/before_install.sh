#!/bin/bash

echo "=== BeforeInstall: Preparing environment ==="

# Docker 및 Docker Compose 설치 확인
if ! command -v docker &> /dev/null; then
    echo "Installing Docker..."
    sudo apt-get update
    sudo apt-get install -y docker.io
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker ubuntu
fi

if ! command -v docker-compose &> /dev/null; then
    echo "Installing Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# 이전 배포 파일 백업
if [ -d /home/ubuntu/toty ]; then
    echo "Backing up previous deployment..."
    sudo rm -rf /home/ubuntu/toty-backup
    sudo cp -r /home/ubuntu/toty /home/ubuntu/toty-backup
fi

# 배포 디렉토리 생성
sudo mkdir -p /home/ubuntu/toty-app
sudo chown -R ubuntu:ubuntu /home/ubuntu/toty-app

echo "Environment preparation completed"
exit 0
