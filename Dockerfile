FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# non-root 사용자 생성 (보안)
RUN groupadd -r spring && useradd -r -g spring spring

# CI에서 빌드된 JAR 복사
COPY build/libs/*.jar app.jar

# Actuator healthcheck용 curl 설치
RUN apt-get update \
 && apt-get install -y curl \
 && rm -rf /var/lib/apt/lists/*

# 애플리케이션 포트
EXPOSE 8070

# 실행 사용자 변경
USER spring:spring

# 애플리케이션 실행
# 프로필은 docker-compose에서 환경변수로 주입
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", \
  "app.jar"]

# 컨테이너 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8070/actuator/health || exit 1
