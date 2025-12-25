#!/bin/bash

echo "=== ApplicationStop: Stopping existing containers ==="

cd /home/ubuntu/toty-app

# 실행 중인 컨테이너 중지
if [ -f docker-compose.yml ]; then
    sudo docker-compose down
    echo "Containers stopped successfully"
else
    echo "docker-compose.yml not found, skipping container stop"
fi

exit 0
