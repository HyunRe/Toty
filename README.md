# TOTY Backend

> 게시글 기반 멘토--멘티 매칭과 실시간 알림 시스템을 구현한 커뮤니티형
> 멘토링 플랫폼 백엔드

TOTY는 멘토링 모집 게시글, 멘토 선정 날짜 스케줄링, 실시간 알림(SSE),
푸시 알림(FCM), 이메일/SMS 발송을 통합 설계한 백엔드 시스템입니다.\
팀 프로젝트로 시작하여 이후 개인 고도화를 통해 ElasticSearch 도입, 캐시
전략 개선, HTTPS 전환 및 인프라 안정화를 수행했습니다.

------------------------------------------------------------------------

# 1. 프로젝트 소개

##  기획 배경

### 문제 정의

-   멘토링 플랫폼에서 멘토 선정이 수동적으로 관리됨
-   선정/박탈 결과에 대한 실시간 알림 체계 부재
-   게시글 기반 커뮤니티와 매칭 로직이 분리되지 않음

### 기존 서비스의 한계

-   멘토 선정 날짜 자동 처리 불가
-   알림 시스템이 실시간이 아님
-   대용량 게시글 조회 시 성능 저하

### 해결 방향

-   멘토 선정 날짜 기반 스케줄 자동화
-   Redis 기반 SSE 실시간 알림 구조 설계
-   FCM + Email + SMS 통합 알림 시스템 구축
-   ElasticSearch 도입으로 검색 성능 개선
-   HTTPS 전환으로 보안 강화

------------------------------------------------------------------------

##  프로젝트 목표

-   게시글 중심 멘토링 플랫폼 설계
-   멘토 자동 선정/박탈 스케줄링 구현
-   Redis 기반 실시간 알림 시스템 구축
-   ElasticSearch 기반 검색 고도화
-   HTTPS 기반 실서비스 수준 보안 환경 구축

------------------------------------------------------------------------

# 2. 기술 스택

  분류        기술

----------- ----------------------

  Language    Java
  Framework   Spring Boot
  Security    Spring Security, JWT
  ORM         JPA (Hibernate)
  Database    MySQL
  Cache       Redis
  Real-Time   SSE, WebSocket
  Search      ElasticSearch
  Push        FCM
  Email/SMS   SMTP, SMS API
  Storage     AWS S3
  Infra       EC2, RDS, Nginx
  DNS/SSL     Route 53, ACM
  CI/CD       GitHub Actions
  Test        JUnit5, Mockito
  Docs        Swagger

------------------------------------------------------------------------

# 3. 시스템 아키텍처

    Client
       ↓
    Spring Boot Server (REST API + Security)
       ↓
    Redis (Cache / PubSub)
    ElasticSearch (Search)
    MySQL (Database)
    AWS S3 (Image Storage)
    FCM / Email / SMS (External Services)

------------------------------------------------------------------------

# 4. 프로젝트 구조

    com.toty
    ├── user/              # 사용자, 인증, 프로필
    ├── post/              # 게시글
    ├── comment/           # 댓글
    ├── chat/              # 실시간 채팅
    ├── notification/      # 알림 (SSE, FCM, Email, SMS)
    ├── mentor/            # 멘토 선정 및 권한 관리
    ├── scheduler/         # 멘토 자동 승급/박탈
    ├── image/             # 이미지 관리 (S3)
    ├── search/            # 검색 (ElasticSearch)
    └── common/            # 공통 설정, 보안, 예외 처리

------------------------------------------------------------------------

# 5. 핵심 기능

##  인증 / 인가

-   JWT 기반 인증 (Access + Refresh Token)
-   Spring Security Filter 기반 토큰 검증

##  핵심 비즈니스 로직

-   게시글/댓글 CRUD
-   멘토 선정 날짜 기반 자동 상태 변경
-   선정/박탈 시 실시간 알림 및 푸시 발송

##  실시간 기능

-   SSE 기반 실시간 알림
-   Redis Pub/Sub 기반 멀티 인스턴스 대응

##  기타 기능

-   ElasticSearch 기반 검색
-   S3 이미지 업로드
-   이메일/SMS 발송

------------------------------------------------------------------------

# 6. 성능 개선 및 설계 고민

## ElasticSearch 도입

-   LIKE 검색 성능 문제 해결
-   검색 응답 속도 개선 및 DB 부하 감소

## Redis 기반 SSE 확장성 개선

-   멀티 인스턴스 환경에서 이벤트 동기화 문제 해결

## HTTPS 전환

-   Nginx Reverse Proxy 구성
-   Route 53 + ACM 인증서 적용
-   HTTP → HTTPS 리다이렉트 설정

------------------------------------------------------------------------

# 7. 테스트 전략

## 단위 테스트

-   도메인 로직 테스트
-   멘토 선정 로직 테스트
-   인증 로직 테스트

## 통합 테스트

-   DB 연동 테스트
-   Redis 연동 테스트
-   SSE 동작 테스트
-   동시성 테스트

------------------------------------------------------------------------

# 8. CI/CD

push / PR → Test → Build → Deploy

-   GitHub Actions 기반 자동 배포

------------------------------------------------------------------------

# 9. 담당 영역

##  팀 프로젝트

-   게시글/댓글 구현
-   멘토 선정 날짜 스케줄 설정
-   Redis 기반 SSE 구축
-   FCM 알림 시스템 구축
-   이메일/SMS 발송 구현

##  개인 고도화

-   ElasticSearch 도입
-   캐시 전략 개선
-   테스트 코드 보강
-   Nginx 기반 HTTPS 전환
-   Route 53 + ACM 연동

------------------------------------------------------------------------

# 10. 실행 방법

## 인프라 실행

    docker-compose up -d

## 애플리케이션 실행

    ./gradlew bootRun

## API 문서

    http://localhost:8080/swagger-ui
