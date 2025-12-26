#!/bin/bash

echo "=== ValidateService: Checking application health ==="
cd /home/ubuntu/toty-app

MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    ATTEMPT=$((ATTEMPT+1))
    echo "Health check attempt $ATTEMPT/$MAX_ATTEMPTS..."

    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8070/actuator/health)

    if [ "$HTTP_STATUS" = "200" ]; then
        echo "✓ Application is healthy (HTTP $HTTP_STATUS)"

        # 모든 컨테이너 상태 확인
        APP_RUNNING=$(sudo docker-compose ps app | grep "Up" | wc -l)
        MYSQL_RUNNING=$(sudo docker-compose ps mysql | grep "Up" | wc -l)
        REDIS_RUNNING=$(sudo docker-compose ps redis | grep "Up" | wc -l)
        ES_RUNNING=$(sudo docker-compose ps elasticsearch | grep "Up" | wc -l)

        if [ "$APP_RUNNING" -eq 1 ] && [ "$MYSQL_RUNNING" -eq 1 ] && [ "$REDIS_RUNNING" -eq 1 ] && [ "$ES_RUNNING" -eq 1 ]; then
            echo "✓ All containers (App, MySQL, Redis, ES) are running"
            echo "=== Deployment successful ==="
            exit 0
        else
            echo "✗ Some containers are not running"
        fi
    else
        echo "✗ Application health check failed (HTTP $HTTP_STATUS)"
    fi
    sleep 10
done

echo "=== Deployment validation failed ==="
sudo docker-compose logs --tail=50 app
exit 1