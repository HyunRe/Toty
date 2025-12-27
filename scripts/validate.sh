#!/bin/bash

echo "=== ValidateService: Checking application health ==="
cd /home/ubuntu/toty-app

# 1. 초기 대기 시간 확보
# 로그상 앱 초기화에 약 32초가 소요되므로, 첫 체크 전 충분히 기다립니다.
echo "Waiting 35 seconds for Spring Boot to warm up..."
sleep 35

MAX_ATTEMPTS=40  # 최대 시도 횟수
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    ATTEMPT=$((ATTEMPT+1))
    echo "Health check attempt $ATTEMPT/$MAX_ATTEMPTS..."

    # 2. Readiness 프로브 체크 (application-prod.yaml에 설정한 경로)
    # 앱이 DB, Redis, ES와 모두 연결되어 서비스 준비가 끝났는지 확인합니다.
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8070/actuator/health/readiness)

    if [ "$HTTP_STATUS" = "200" ]; then
        echo "✓ Application Readiness Check Passed (HTTP $HTTP_STATUS)"

        # 3. Docker 컨테이너 실행 상태 확인 (프로세스 레벨)
        # docker-compose ps 결과에서 'Up' 상태인 컨테이너 개수를 체크합니다.
        APP_RUNNING=$(sudo docker-compose ps app | grep -E "Up|running" | wc -l)
        MYSQL_RUNNING=$(sudo docker-compose ps mysql | grep -E "Up|running" | wc -l)
        REDIS_RUNNING=$(sudo docker-compose ps redis | grep -E "Up|running" | wc -l)
        ES_RUNNING=$(sudo docker-compose ps elasticsearch | grep -E "Up|running" | wc -l)

        if [ "$APP_RUNNING" -ge 1 ] && [ "$MYSQL_RUNNING" -ge 1 ] && [ "$REDIS_RUNNING" -ge 1 ] && [ "$ES_RUNNING" -ge 1 ]; then
            echo "✓ All containers (App, MySQL, Redis, ES) are confirmed running."
            echo "=== Deployment successful ==="
            exit 0
        else
            echo "✗ Network response is OK, but some containers are not in 'Up' state."
            sudo docker-compose ps
        fi
    else
        # 아직 준비가 안 된 경우 (Connection reset 포함) 에러를 뱉지 않고 재시도합니다.
        echo "✗ Application is not ready yet (HTTP Status: $HTTP_STATUS). Retrying in 10s..."
    fi

    sleep 10
done

# 최종 실패 시 로그 출력
echo "=== Deployment validation failed after $MAX_ATTEMPTS attempts ==="
echo "Showing last 50 lines of application logs:"
sudo docker-compose logs --tail=50 app
exit 1