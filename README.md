# Toty - 멘토링 & 지식 공유 플랫폼

멘토와 학습자를 연결하는 커뮤니티 플랫폼입니다. 게시글, 댓글, 실시간 채팅, 알림 기능을 통해 지식을 공유하고 소통할 수 있습니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Spring Boot 3.4.1, Java 17 |
| Database | MySQL 8.0, Redis, H2 (테스트) |
| Search | Elasticsearch 8.17 |
| Auth | JWT (Access/Refresh Token), OAuth2 (Kakao, GitHub, Google) |
| Real-Time | WebSocket (STOMP), SSE, Redis Pub/Sub |
| Cloud | AWS S3 (이미지 업로드) |
| Notification | FCM, Email (Gmail SMTP), SMS (Nurigo) |
| API Docs | SpringDoc OpenAPI 2.5.0 |
| Test | JUnit 5, Mockito, AssertJ |

## 프로젝트 구조

```
src/main/java/com/toty/
├── common/          # 공통 모듈 (보안, 설정, 예외, Redis, SSE, 이미지 등)
├── chat/            # 실시간 멘토링 채팅
├── comment/         # 댓글
├── following/       # 팔로우/팔로잉
├── notification/    # 알림 (SSE, FCM, Email, SMS)
├── post/            # 게시글 (일반, 지식, Q&A)
├── roleRefreshScheduler/  # 멘토 자동 승급/강등 스케줄러
└── user/            # 사용자 관리
```

**아키텍처**: Layered Architecture + DDD

- **Presentation**: REST API, View, SSE, STOMP Controller
- **Application**: Service, 트랜잭션 관리, DTO 변환
- **Domain**: Entity, Value Object, Repository, Domain Event, Strategy/Factory 패턴
- **Infrastructure**: JPA, Redis, S3, Firebase, Elasticsearch, WebSocket

## 주요 기능

### 사용자
- 회원가입/로그인 (JWT + OAuth2)
- 프로필 관리 (닉네임, 프로필 이미지, 상태 메시지)
- 전화번호 인증 (SMS)
- 역할 시스템 (USER → MENTOR → ADMIN)

### 게시글
- 카테고리별 게시글 (일반, 지식, Q&A)
- 좋아요, 스크랩
- Elasticsearch 전문 검색
- 페이지네이션

### 댓글
- 댓글 CRUD
- SSE를 통한 실시간 댓글 업데이트

### 채팅
- WebSocket 기반 실시간 멘토링 채팅방
- 채팅방 생성 (멘토 전용), 참여, 메시지 히스토리

### 팔로우
- 팔로우/언팔로우
- 팔로워 100명 이상 시 멘토 자동 승급 (스케줄러)

### 알림
- SSE 실시간 알림
- FCM 푸시 알림
- Email / SMS 알림
- 댓글, 좋아요, 팔로우, 멘토 승급/강등 알림

## 실행 방법

### 사전 요구사항

- Java 17+
- MySQL 8.0+
- Redis 6.0+
- Elasticsearch 8.17
- Gradle 8.0+

### 1. 데이터베이스 설정

```sql
CREATE DATABASE toty;
```

### 2. 시크릿 설정

`src/main/resources/application-secret.yaml` 파일을 생성하고 아래 항목을 설정합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/toty
    username: root
    password: <비밀번호>

redis:
  password: <Redis 비밀번호>

cloud:
  aws:
    credentials:
      access-key: <AWS_ACCESS_KEY>
      secret-key: <AWS_SECRET_KEY>
    s3:
      bucket: <S3_BUCKET_NAME>

spring:
  mail:
    username: <이메일>
    password: <앱 비밀번호>
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: <KAKAO_CLIENT_ID>
            client-secret: <KAKAO_CLIENT_SECRET>
          google:
            client-id: <GOOGLE_CLIENT_ID>
            client-secret: <GOOGLE_CLIENT_SECRET>
          github:
            client-id: <GITHUB_CLIENT_ID>
            client-secret: <GITHUB_CLIENT_SECRET>

sms:
  api-key: <NURIGO_API_KEY>

firebase:
  admin-key: <Firebase JSON 경로>
```

### 3. 빌드 및 실행

```bash
# 빌드
./gradlew clean build -x test

# 실행 (http://localhost:8070)
./gradlew bootRun
```

### 4. 확인

- Health Check: http://localhost:8070/actuator/health
- Swagger UI: http://localhost:8070/swagger-ui
- OpenAPI: http://localhost:8070/v3/api-docs

## 테스트

```bash
# 단위 테스트
./gradlew unitTest

# 통합 테스트
./gradlew integrationTest

# 전체 테스트 (unit → integration → E2E)
./gradlew check
```

| 구분 | 위치 | 설명 |
|------|------|------|
| Unit Test | `src/test_unit/` | Service, Value Object, 비동기 처리 등 단위 테스트 (Mockito) |
| Integration Test | `src/test_integration/` | JPA 연관관계, N+1 쿼리 검증 (H2 인메모리 DB) |
| API E2E Test | `src/test_apiE2E/` | REST API 엔드포인트 테스트 |

## 성능 최적화

| 대상 | 개선 전 | 개선 후 | 개선율 |
|------|---------|---------|--------|
| 게시글 목록 (100건) | 101 쿼리 | 1 쿼리 | 99% |
| 팔로잉 목록 (100건) | 201 쿼리 | 1 쿼리 | 99.5% |
| 유저 정보 (캐시) | 100ms | 5ms | 95% |
| S3 삭제 포함 요청 | 2.5s | 0.5s | 80% |

주요 최적화: Fetch Join으로 N+1 해결, Redis 캐싱, 비동기 처리 (`@Async`)

## 적용 디자인 패턴

- **Strategy**: 게시글 카테고리별 생성/수정 전략
- **Factory**: 게시글 생성/수정 팩토리
- **Builder**: Entity 생성 (Post, Comment, User)
- **Value Object**: Email, PhoneNumber, Nickname
- **Domain Event**: 이벤트 기반 알림 처리
- **CQRS**: 채팅 서비스 분리 (Query / Messaging)
