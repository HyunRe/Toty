# Toty

멘토링 & 지식 공유 플랫폼

멘토와 학습자를 연결하는 커뮤니티 플랫폼입니다. 게시글, 댓글, 실시간 채팅, 알림 기능을 통해 지식을 공유하고 소통할 수 있습니다.

---

## 1. 프로젝트 소개

### 기획 배경

**문제 정의**
- 기존 멘토링 플랫폼은 일회성 매칭에 그쳐 지속적인 관계 형성이 어려움
- 질문과 답변이 분리되어 있어 지식 축적 및 공유가 비효율적
- 실시간 소통 채널의 부재로 즉각적인 피드백이 어려움

**기존 서비스의 한계**
- 단순 Q&A 게시판 형태로 멘토-멘티 관계 구축 어려움
- 알림 시스템 미흡으로 중요 활동 놓침
- 멘토 자격 관리 부재

**해결 방향**
- 팔로우 기반 멘토-멘티 관계 형성
- 실시간 채팅과 알림으로 즉각적인 소통 지원
- 팔로워 수 기반 자동 멘토 승급 시스템

### 프로젝트 목표

1. **지식 공유 플랫폼 구축**: 일반, 지식, Q&A 카테고리별 게시글 시스템
2. **실시간 소통 지원**: WebSocket 채팅, SSE 알림
3. **멘토링 생태계 조성**: 팔로우 기반 멘토 승급, 멘토 전용 채팅방
4. **다양한 알림 채널**: SSE, FCM 푸시, Email, SMS 알림
5. **고성능 시스템**: N+1 쿼리 최적화, Redis 캐싱, 비동기 처리

---

## 2. 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1, Spring Security, Spring Data JPA |
| Security | JWT (Access/Refresh Token), OAuth2 (Kakao, GitHub, Google), BCrypt |
| ORM | JPA/Hibernate |
| Database | MySQL 8.0 |
| Cache | Redis (캐싱, Pub/Sub, 세션, 토큰 블랙리스트) |
| Search | Elasticsearch 8.17 |
| Real-Time | WebSocket (STOMP), SSE (Server-Sent Events), Redis Pub/Sub |
| Push | FCM (Firebase Cloud Messaging) |
| Notification | Email (Gmail SMTP), SMS (Nurigo) |
| Storage | AWS S3 |
| Infra | Docker, AWS |
| Test | JUnit 5, Mockito, AssertJ, H2 |
| Docs | SpringDoc OpenAPI 2.5.0 (Swagger) |

---

## 3. 시스템 아키텍처

```
Client (Web/Mobile)
        ↓
   API Server (Spring Boot)
        ↓
   ┌────┴────┐
   ↓         ↓
Redis     MySQL
(Cache)   (Primary DB)
   ↓
Elasticsearch
(Full-text Search)
   ↓
External Services
├── AWS S3 (Image Storage)
├── FCM (Push Notification)
├── Gmail SMTP (Email)
└── Nurigo (SMS)
```

### 설계 원칙

1. **Layered Architecture + DDD**: Presentation → Application → Domain → Infrastructure 계층 분리
2. **도메인 중심 설계**: Value Object, Domain Event, Strategy/Factory 패턴 적용
3. **CQRS 패턴**: 조회(Query)와 명령(Command) 서비스 분리
4. **이벤트 기반 아키텍처**: Domain Event를 통한 느슨한 결합
5. **캐시 우선 전략**: Redis 캐싱으로 DB 부하 감소

---

## 4. 프로젝트 구조

```
com.toty
├── user/                    # 사용자 관리
│   ├── application/         # UserService, UserInfoService
│   ├── domain/              # User 엔티티, Value Objects (Email, PhoneNumber, Nickname)
│   ├── dto/                 # Request/Response DTO
│   └── presentation/        # API, View Controller
├── post/                    # 게시글 (일반, 지식, Q&A)
│   ├── application/         # PostService, PostPaginationService, ElasticSearch
│   ├── domain/              # Post 엔티티, Strategy, Factory, Repository
│   ├── dto/
│   └── presentation/
├── comment/                 # 댓글
│   ├── application/         # CommentService, SSE
│   ├── domain/              # Comment 엔티티
│   └── presentation/
├── chat/                    # 실시간 채팅
│   ├── application/         # ChatRoomService, QueryService, MessagingService
│   ├── domain/              # ChatRoom, ChatMessage, ChatParticipant
│   ├── infrastructure/      # WebSocket 설정
│   └── presentation/        # API, STOMP, SSE Controller
├── following/               # 팔로우/팔로잉
├── notification/            # 알림 (SSE, FCM, Email, SMS)
│   ├── application/
│   ├── domain/
│   └── infrastructure/      # FCM, Email, SMS Sender
├── roleRefreshScheduler/    # 멘토 자동 승급/강등 스케줄러
└── common/                  # 공통 모듈
    ├── config/              # AsyncConfig, WebMvc
    ├── security/            # JWT, OAuth2, Filter
    ├── redis/               # RedisConfig, RedisService
    ├── sse/                 # SSE 인프라
    ├── event/               # DomainEvent, EventPublisher
    ├── image/               # S3 이미지 업로드
    ├── exception/           # 예외 처리
    └── monitoring/          # 헬스 체크, 장애 알림
```

---

## 5. 핵심 기능

### 인증 / 인가

**로그인 방식**
- Form Login (Email/Password)
- OAuth2 Social Login (Kakao, GitHub, Google)

**토큰 전략**
- Access Token: 30분, httpOnly=false (클라이언트 접근 필요)
- Refresh Token: 7일, httpOnly=true (보안 강화)
- Token Rotation: Refresh 사용 시 새 토큰 발급, 기존 토큰 블랙리스트 등록

**인증 처리 구조**
```
Request → JwtRequestFilter → 토큰 추출 → 블랙리스트 체크 → 유효성 검증 → SecurityContext 설정
```

### 게시글 시스템

| 카테고리 | 설명 | 특징 |
|---------|------|------|
| GENERAL | 일반 게시글 | 자유로운 주제 |
| INFORMATION | 지식 공유 | 튜토리얼, 노하우 |
| QNA | 질문과 답변 | 문제 해결 |

- **Strategy 패턴**: 카테고리별 생성/수정 전략 분리
- **Factory 패턴**: PostCreationFactory, PostUpdateFactory
- **Elasticsearch**: 제목, 내용 전문 검색

### 실시간 기능

**WebSocket 채팅**
- STOMP 프로토콜 기반 실시간 메시징
- Redis Pub/Sub으로 다중 서버 지원
- 멘토 전용 채팅방 생성

**SSE (Server-Sent Events)**
- 실시간 댓글 업데이트
- 실시간 알림 전송
- 채팅방 목록 실시간 갱신

### 알림 시스템

| 채널 | 용도 | 특징 |
|------|------|------|
| SSE | 실시간 알림 | 웹 브라우저 푸시 |
| FCM | 모바일 푸시 | Firebase 기반 |
| Email | 중요 알림 | 멘토 승급/강등, 비밀번호 재설정 |
| SMS | 인증 | 전화번호 인증 코드 |

**알림 트리거**
- 새 댓글, 좋아요, 팔로우
- 멘토 승급/강등
- 시스템 장애 알림

### 멘토 시스템

- **자동 승급**: 팔로워 100명 이상 → MENTOR 역할 부여
- **자동 강등**: 팔로워 100명 미만 → USER 역할로 변경
- **스케줄러**: 주기적으로 팔로워 수 체크 및 역할 갱신
- **멘토 전용 기능**: 채팅방 생성

---

## 6. 성능 개선 및 설계 고민

### 6-1. N+1 쿼리 문제 해결

**문제**
```java
// 기존: Post 100개 조회 시 User 조회 쿼리 100번 추가 발생
Page<Post> posts = postRepository.findAll(pageable);
posts.forEach(p -> p.getUser().getNickname()); // N+1 발생
// 총 쿼리: 1(Post) + 100(User) = 101개
```

**해결**
```java
// fetch join으로 단일 쿼리로 해결
@Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user")
Page<Post> findAllWithUser(Pageable pageable);
// 총 쿼리: 1개
```

**개선 결과**

| Repository | 개선 전 | 개선 후 | 개선율 |
|-----------|--------|--------|-------|
| PostRepository | 101 쿼리 | 1 쿼리 | 99% |
| FollowingRepository | 201 쿼리 | 1 쿼리 | 99.5% |
| PostLikeRepository | 101 쿼리 | 1 쿼리 | 99% |
| ChatRoomRepository | 11 쿼리 | 1 쿼리 | 91% |

### 6-2. 캐시 전략

**캐시 대상**
- 사용자 정보 (UserInfo)

**Key 전략**
```
userInfo::{userId}
```

**TTL 전략**
- 1시간 (3600초)
- 사용자 정보 수정 시 즉시 캐시 무효화 (`@CacheEvict`)

**기대 효과**
- 캐시 히트 시 응답 시간: 100ms → 5ms (95% 개선)
- DB 부하 제로

### 6-3. 비동기 처리

**문제**
- S3 이미지 삭제 시 동기 처리로 응답 지연 (2.5초)

**해결**
- `@Async` 어노테이션으로 비동기 처리
- ThreadPool: Core 2, Max 5, Queue 100

**개선 결과**
- 응답 시간: 2.5초 → 0.5초 (80% 개선)

### 6-4. 트랜잭션 전략

**읽기/쓰기 분리**
- 조회 메서드: `@Transactional(readOnly = true)`
- 변경 메서드: `@Transactional`

**범위 최소화**
- 트랜잭션 내에서 외부 API 호출 지양
- S3, FCM 등 외부 서비스는 트랜잭션 외부에서 비동기 처리

---

## 7. 테스트 전략

### 테스트 구조

```
src/test/resources/          # 공유 설정 (application-test.yaml)
src/test_unit/java/com/toty/
├── domain/                  # Value Object 테스트
├── service/                 # Service 단위 테스트
├── infrastructure/          # S3, FCM, SMS 테스트
└── util/                    # 유틸리티 테스트
src/test_integration/java/com/toty/
├── TestJpaApplication.java  # 통합 테스트용 설정
└── repository/              # Repository 통합 테스트
src/test_apiE2E/java/        # API E2E 테스트
```

### 단위 테스트

- **도메인 테스트**: Email, PhoneNumber, Nickname Value Object 검증
- **서비스 테스트**: Mockito를 활용한 비즈니스 로직 테스트
- **인프라 테스트**: S3StorageService, FCM, SMS 발송 테스트
- **비동기 테스트**: @Async 메서드 동작 검증

### 통합 테스트

- **DB 연동 테스트**: H2 인메모리 DB 사용
- **N+1 검증**: Hibernate Statistics로 쿼리 수 측정
- **연관관계 테스트**: CASCADE, 영속성 전이 검증

### 테스트 실행

```bash
# 단위 테스트
./gradlew unitTest

# 통합 테스트
./gradlew integrationTest

# 전체 테스트 (unit → integration → E2E)
./gradlew check
```

---

## 8. 트러블슈팅

### 1. N+1 쿼리로 인한 성능 저하

**문제 상황**
- 게시글 100개 조회 시 101개의 쿼리 발생
- 팔로잉 목록 조회 시 201개의 쿼리 발생

**원인**
- JPA 지연 로딩(LAZY)으로 연관 엔티티 접근 시마다 추가 쿼리 발생

**해결 방법**
- `LEFT JOIN FETCH`로 연관 엔티티 즉시 로딩
- 페이징 시 `countQuery` 분리

### 2. JWT 토큰 탈취 시 보안 취약점

**문제 상황**
- 로그아웃 후에도 탈취된 토큰으로 API 접근 가능

**원인**
- JWT는 Stateless하여 서버에서 무효화 불가

**해결 방법**
- Redis 기반 토큰 블랙리스트 구현
- Refresh Token Rotation으로 토큰 재사용 방지
- 블랙리스트 TTL = 토큰 만료 시간

### 3. S3 이미지 삭제로 인한 응답 지연

**문제 상황**
- 게시글 삭제 시 S3 이미지 삭제 대기로 2.5초 지연

**원인**
- 동기 처리로 S3 API 응답 대기

**해결 방법**
- `@Async`로 비동기 처리
- 메인 트랜잭션과 분리하여 S3 실패 시에도 게시글 삭제 성공

---

## 9. 실행 방법

### 1. 인프라 실행

```bash
# MySQL
docker run -d --name mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=toty \
  mysql:8.0

# Redis
docker run -d --name redis -p 6379:6379 redis:latest

# Elasticsearch
docker run -d --name elasticsearch -p 9200:9200 \
  -e "discovery.type=single-node" \
  elasticsearch:8.17.0
```

### 2. 시크릿 설정

`src/main/resources/application-secret.yaml` 생성:

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

### 3. 애플리케이션 실행

```bash
# 빌드
./gradlew clean build -x test

# 실행
./gradlew bootRun
```

### 4. API 문서

- Swagger UI: http://localhost:8070/swagger-ui
- OpenAPI JSON: http://localhost:8070/v3/api-docs
- Health Check: http://localhost:8070/actuator/health

---

## 프로젝트를 통해 얻은 경험

1. **N+1 쿼리 최적화**: Fetch Join, Hibernate Statistics를 활용한 쿼리 성능 분석 및 개선
2. **JWT 보안 강화**: 토큰 블랙리스트, Rotation 전략으로 Stateless 인증의 보안 취약점 보완
3. **실시간 통신 구현**: WebSocket(STOMP), SSE, Redis Pub/Sub을 활용한 실시간 기능 구현
4. **DDD 패턴 적용**: Value Object, Domain Event, Strategy/Factory 패턴으로 도메인 로직 캡슐화
5. **테스트 인프라 구축**: 단위/통합/E2E 테스트 분리 및 자동화

---

## 적용 디자인 패턴

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **Strategy** | PostCreationStrategy | 카테고리별 게시글 생성 전략 |
| **Factory** | PostCreationFactory | 전략 선택 및 객체 생성 |
| **Builder** | Post, Comment, User | 엔티티 생성 가독성 향상 |
| **Value Object** | Email, PhoneNumber, Nickname | 도메인 규칙 캡슐화 |
| **Domain Event** | DomainEventPublisher | 이벤트 기반 느슨한 결합 |
| **CQRS** | ChatRoomQueryService / ChatRoomService | 조회/명령 분리 |
