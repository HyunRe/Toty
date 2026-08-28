# TOTY Backend

게시글 기반 멘토–멘티 매칭과 실시간 알림 시스템을 구현한 커뮤니티형 멘토링 플랫폼 백엔드

TOTY는 멘토링 모집 게시글, 멘토 선정 날짜 스케줄링, 실시간 알림(SSE), 푸시 알림(FCM), 이메일/SMS 발송을 통합 설계한 백엔드 시스템입니다.
팀 프로젝트로 시작하여 이후 개인 고도화를 통해 ElasticSearch 도입, 캐시 전략 개선, HTTPS 전환 및 인프라 안정화를 수행했습니다.

---

## 1. 프로젝트 소개

### 기획 배경

#### 문제 정의

- 멘토링 플랫폼에서 멘토 선정이 수동적으로 관리됨
- 선정/박탈 결과에 대한 실시간 알림 체계 부재
- 게시글 기반 커뮤니티와 매칭 로직이 분리되지 않음

#### 기존 서비스의 한계

- 멘토 선정 날짜 자동 처리 불가
- 알림 시스템이 실시간이 아님
- 대용량 게시글 조회 시 성능 저하

#### 해결 방향

- 멘토 선정 날짜 기반 스케줄 자동화
- Redis 기반 SSE 실시간 알림 구조 설계
- FCM + Email + SMS 통합 알림 시스템 구축
- ElasticSearch 도입으로 검색 성능 개선
- HTTPS 전환으로 보안 강화

---

### 프로젝트 목표

- 게시글 중심 멘토링 플랫폼 설계
- 멘토 자동 선정/박탈 스케줄링 구현
- Redis 기반 실시간 알림 시스템 구축
- ElasticSearch 기반 검색 고도화
- HTTPS 기반 실서비스 수준 보안 환경 구축

---

## 2. 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Security | Spring Security, JWT (jjwt), OAuth2 (Kakao, GitHub, Google) |
| ORM | Spring Data JPA |
| Database | MySQL 8.0 |
| Cache | Redis (캐싱, Pub/Sub, 토큰 블랙리스트) |
| Search | Elasticsearch 8.17 |
| Real-Time | WebSocket (STOMP), SSE (Server-Sent Events) |
| Push | Firebase Cloud Messaging |
| Notification | Email (Gmail SMTP), SMS (Nurigo) |
| Storage | AWS S3 |
| Infra | EC2, RDS, Nginx, Docker |
| DNS/SSL | Route 53, ACM |
| CI/CD | GitHub Actions |
| Test | JUnit5, Mockito, AssertJ, H2 |
| Docs | Swagger (springdoc-openapi) |

---

## 3. 시스템 아키텍처

```
                          ┌──────────┐
                          │  Client  │
                          │ (Web/App)│
                          └────┬─────┘
                               │
                               ▼
                   ┌──────────────────────────┐
                   │   Spring Boot Server     │
                   │ (REST API + Security)    │
                   └───────────┬──────────────┘
                               │
 ┌────────────┬───────────────┼────────────┬─────────────┬────────────┬────────────┐
 │            │               │            │             │            │            │
 ▼            ▼               ▼            ▼             ▼            ▼            ▼
┌─────────┐ ┌──────────┐ ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐ ┌────────┐
│  Redis  │ │  Elastic │ │  MySQL  │ │  AWS S3  │ │  Email   │ │  SMS   │ │  FCM   │
│ (Cache) │ │  Search  │ │  (DB)   │ │ (Image)  │ │ Service  │ │ Service│ │ (Push) │
└─────────┘ └──────────┘ └─────────┘ └──────────┘ └──────────┘ └────────┘ └────────┘
      │
      ▼
┌────────────┐
│ SSE Server │
└────────────┘
```

### 설계 원칙

- **DDD 기반 패키지 구조**
    - 도메인별 패키지 분리 (user, post, comment, chat, notification, following 등)
    - 계층 분리: application / domain / presentation / infrastructure / dto
- **계층별 역할 분리**
    - Controller → 요청/응답만 담당
    - Service → 비즈니스 로직 집중
    - Entity → 도메인 로직 캡슐화 (상태 전이, 유효성 검증)
- **인프라 연동**
    - Redis → 사용자 정보 캐싱, 토큰 블랙리스트, Pub/Sub
    - Elasticsearch → 게시글/댓글 전문 검색
    - FCM/Email/SMS → 다채널 알림
- **상태 관리**
    - Enum 기반 상태 관리 (Role, PostCategory, EventType 등)
    - Value Object 패턴 (Email, PhoneNumber, Nickname)

---

## 4. 프로젝트 구조

```
com.toty
├── user/              # 사용자, 인증, 프로필
├── post/              # 게시글 (모집글, Q&A)
├── comment/           # 댓글
├── chat/              # 실시간 채팅 (WebSocket + Redis Pub/Sub)
├── following/         # 팔로우/팔로잉
├── notification/      # 알림 (SSE, FCM, Email, SMS)
├── roleRefreshScheduler/  # 멘토 자동 승급/강등 스케줄러
├── image/             # 이미지 관리 (S3)
├── search/            # 검색 (ElasticSearch)
└── common/            # 공통 설정, 보안, 예외 처리, Redis, SSE
```

---

## 5. 핵심 기능

### 인증 / 인가

- Form Login + OAuth2 소셜 로그인 (Kakao, GitHub, Google)
- JWT Access Token (30분)
- JWT Refresh Token (7일, httpOnly)
- Spring Security 필터 체인 기반 인증 처리
- Refresh Token Rotation + 블랙리스트 구조 설계

---

### 멘토 자동 승급 시스템

팔로워 수 기반으로 멘토 역할을 자동 부여/해제

| 조건 | 결과 |
|------|------|
| 팔로워 100명 이상 | MENTOR 역할 부여 |
| 팔로워 100명 미만 | USER 역할로 변경 |

- 스케줄러가 주기적으로 팔로워 수 체크
- 역할 변경 시 Email/FCM 알림 발송
- 멘토 전용 기능: 채팅방 생성

---

### 실시간 채팅 시스템

- WebSocket (STOMP) 기반 양방향 통신
- Redis Pub/Sub으로 다중 서버 지원
- 멘토 전용 채팅방 생성
- 채팅방 참여/퇴장 관리
- 메시지 히스토리 저장

---

### 다채널 알림 시스템

| 채널 | 용도 | 특징 |
|------|------|------|
| SSE | 실시간 알림 | 웹 브라우저 실시간 푸시 |
| FCM | 모바일 푸시 | Firebase 기반 |
| Email | 중요 알림 | 멘토 승급/강등, 비밀번호 재설정 |
| SMS | 인증 | 전화번호 인증 코드 |

**알림 트리거**
- 새 댓글, 좋아요, 팔로우
- 멘토 승급/강등
- 시스템 장애 알림

---

### 게시글 시스템

| 카테고리 | 설명 |
|---------|------|
| GENERAL | 일반 게시글 (자유 주제) |
| KNOWLEDGE | 지식 공유 (튜토리얼, 노하우) |
| QNA | 질문과 답변 |

- Strategy 패턴으로 카테고리별 생성/수정 전략 분리
- Factory 패턴으로 전략 선택 및 객체 생성
- Elasticsearch 전문 검색

---

## 6. 핵심 비즈니스 로직

### 1. 게시글 & 댓글

- 멘토링 모집 게시글 CRUD
- 댓글/대댓글 구조 설계
- 게시글 작성자 기준 멘토 선정 가능
- 멘토 선정 시 권한 상태 자동 변경

---

### 2. 멘토 선정 & 박탈

- 선정 날짜 도달 시 자동 상태 변경 (Scheduler 기반)
- 단일 트랜잭션 처리로 데이터 정합성 보장
- 멘토 권한 자동 부여 / 박탈
- 선정/박탈 시 실시간 알림 + 푸시 + 이메일 + SMS 발송
- 멘토 상태 변경 시 관련 채팅방 접근 권한 자동 갱신

---

### 3. 알림 시스템 (핵심 설계)

#### 실시간 알림 구조

- Redis Pub/Sub 기반 이벤트 브로드캐스트
- SSE(Server-Sent Events)로 실시간 전송
- 멀티 인스턴스 환경 대응

#### 온라인 / 오프라인 처리 전략

- 온라인 사용자 → SSE 즉시 전송
- 오프라인 사용자 → FCM 푸시 발송
- 중요 이벤트 → 이메일 / SMS 추가 발송

#### 알림 발생 이벤트

- 멘토 선정 / 박탈
- 채팅 메시지 수신
- 댓글 작성
- 게시글 관련 상태 변경

#### 설계 특징

- 이벤트 기반 구조
- 비동기 처리 분리
- 알림 실패 시 재시도 전략 고려
- 알림 도메인 독립 설계로 확장 가능 구조 유지

---

### 4. 멘토 중심 채팅방 시스템

#### 채팅 구조

- WebSocket 기반 실시간 양방향 통신
- 멘토링 모집 게시글 단위 채팅방 생성
- 멘토 선정 이후에만 채팅방 활성화

#### 권한 기반 채팅 로직

- 멘토 + 선정된 멘티만 채팅 가능
- 멘토 박탈 시 채팅 권한 자동 제거
- 채팅방 접근 시 사용자 권한 검증

#### 실시간 처리

- 메시지 수신 즉시 브로드캐스트
- 오프라인 사용자에게는 푸시 알림 연동
- 채팅 메시지 저장 후 전송 (영속성 보장)

#### 확장 고려 사항

- Redis 활용 확장 가능 구조
- 채팅 이벤트 → 알림 시스템과 연동
- 멀티 인스턴스 환경에서 메시지 동기화 고려

---

## 7. 성능 개선 및 설계 고민

### 1) N+1 쿼리 해결

**문제**
- 게시글 100개 조회 시 101개의 쿼리 발생
- 팔로잉 목록 조회 시 201개의 쿼리 발생

**해결**
- `LEFT JOIN FETCH`로 연관 엔티티 즉시 로딩
- 페이징 시 `countQuery` 분리

**개선 결과**

| Repository | 개선 전 | 개선 후 | 개선율 |
|-----------|--------|--------|-------|
| PostRepository | 101 쿼리 | 1 쿼리 | 99% |
| FollowingRepository | 201 쿼리 | 1 쿼리 | 99.5% |
| PostLikeRepository | 101 쿼리 | 1 쿼리 | 99% |
| ChatRoomRepository | 11 쿼리 | 1 쿼리 | 91% |

---

### 2) Redis 캐시 전략

- 사용자 정보 조회 결과 캐싱
- Cache Key: `userInfo::{userId}`
- TTL: 1시간
- 사용자 정보 수정 시 즉시 캐시 무효화 (`@CacheEvict`)
- 캐시 히트 시 응답 시간: 100ms → 5ms (95% 개선)

---

### 3) 비동기 처리

#### 3-1) S3 이미지 삭제 비동기화

**문제**
- S3 이미지 삭제 시 동기 처리로 응답 지연 (2.5초)

**해결**
- `@Async("taskExecutor")` 어노테이션으로 비동기 처리
- ThreadPool: Core 2, Max 5, Queue 100

**개선 결과**
- 응답 시간: 2.5초 → 0.5초 (80% 개선)

#### 3-2) 알림 발송 비동기화

**문제**
- 멘토가 지식 게시글을 작성하면 팔로워 전원에게 FCM을 순차 발송
- 외부 API(FCM / SMTP / SMS) 왕복 시간이 요청 스레드에 그대로 누적
- 멘토 선정 알림은 Email + SMS를 순차 발송하여 1.5초 이상 소요

**해결**
- 알림 전용 스레드풀 분리 (`notificationExecutor`: Core 5, Max 10, Queue 25)
- 발송 요청 → `ApplicationEventPublisher` 이벤트 발행 후 즉시 반환
- 이벤트 리스너 · 채널별 Sender(SSE / FCM / Email / SMS) 전 구간 `@Async` 적용
- 개별 채널 실패가 다른 채널 발송에 전파되지 않도록 Sender 단위 예외 격리

**개선 결과** (팔로워 10명 기준 예상치)
- 멘토 게시글 작성 응답: 약 2.1초 → 약 0.06초
- 멘토 선정 알림(Email + SMS): 약 1.5초 → 즉시 반환
- 외부 API 장애 시에도 게시글 작성 자체는 정상 완료

#### 3-3) 알림 전송 실패 재시도

**문제**
- 수신자의 SSE 연결이 없거나 전송이 실패하면 알림이 유실됨

**해결**
- `NotificationRetryQueue`에 실패 건 적재 후 스케줄러가 주기적으로 재전송
- 지수 백오프 적용 (초기 2초, 시도마다 2배, 상한 60초)
- 최대 시도 초과 시 영구 실패로 분류하여 로그 기록

**개선 결과**
- 일시적 네트워크 오류 · 미접속 상태의 알림 유실 방지

---

### 4) JWT 보안 강화

**문제**
- 로그아웃 후에도 탈취된 토큰으로 API 접근 가능

**해결**
- Redis 기반 토큰 블랙리스트 구현
- Refresh Token Rotation으로 토큰 재사용 방지
- 블랙리스트 TTL = 토큰 만료 시간

---

### 5) ElasticSearch 도입

**문제**
- 게시글 LIKE 검색 시 성능 저하

**해결**
- ElasticSearch 도입
- 검색 전용 인덱스 분리

**결과**
- 검색 응답 속도 개선
- DB 부하 감소

---

### 6) Redis 기반 SSE 확장성 개선

**문제**
- 멀티 인스턴스 환경에서 SSE 동기화 불가

**해결**
- Redis Pub/Sub 도입
- 인스턴스 간 이벤트 브로드캐스트

---

### 7) HTTPS 전환

**문제**
- HTTP 환경 보안 취약

**해결**
- Nginx Reverse Proxy 구성
- 도메인 연결 (가비아)
- Route 53 DNS 설정
- ACM 인증서 발급
- HTTP → HTTPS 리다이렉트 설정

---

## 8. 테스트 전략

### 테스트 구조

```
src/test_unit/          # 단위 테스트 (12개)
src/test_integration/   # 통합 테스트 (3개)
src/test_apiE2E/        # API E2E 테스트
```

Gradle Custom SourceSet으로 분리 관리

### 단위 테스트 (12개)

| 파일 | 대상 |
|------|------|
| `EmailTest` | 이메일 형식 및 유효성 검증 |
| `NicknameTest` | 닉네임 검증 로직 |
| `PhoneNumberTest` | 전화번호 검증 로직 |
| `FcmNotificationSenderTest` | Firebase FCM 알림 전송 로직 |
| `S3StorageServiceTest` | AWS S3 업로드/삭제 로직 |
| `SseNotificationSenderTest` | SSE 알림 전송 로직 |
| `ImageUploadServiceTest` | 이미지 업로드 처리 로직 |
| `NotificationCreationServiceTest` | 알림 생성 로직 |
| `NotificationServiceTest` | 알림 비즈니스 로직 |
| `RoleRefreshSchedulerTest` | 역할 갱신 스케줄러 로직 |
| `ImageValidatorTest` | 이미지 유효성 검증 |
| `S3KeyGeneratorTest` | S3 Key 생성 규칙 |

### 통합 테스트 (3개)

| 파일 | 대상 |
|------|------|
| `NPlusOnePerformanceTest` | N+1 문제 성능 검증 |
| `PostCommentIntegrationTest` | 게시글 댓글 DB 연동 테스트 |
| `PostImageIntegrationTest` | 게시글 이미지 DB 연동 테스트 |

### 실행

```bash
# 단위 테스트
./gradlew unitTest

# 통합 테스트
./gradlew integrationTest

# 전체 테스트 (unit → integration → E2E)
./gradlew check
```

---

## 9. CI/CD

```
┌─────────────────────────────────────────────────────────────────┐
│                        GitHub Actions                           │
├─────────────────────────────────────────────────────────────────┤
│  push / PR (main, develop)                                      │
│           ↓                                                     │
│  ┌─────────────────┐                                            │
│  │   Test Stage    │                                            │
│  │  ┌───────────┐  │                                            │
│  │  │ unitTest  │  │                                            │
│  │  └───────────┘  │                                            │
│  │  ┌─────────────────────────┐                                 │
│  │  │    integrationTest      │                                 │
│  │  │  (Testcontainers MySQL) │                                 │
│  │  │  (CI Service: Redis)    │                                 │
│  │  │  (Testcontainers ES)    │                                 │
│  │  └─────────────────────────┘                                 │
│  └─────────────────┘                                            │
│           ↓ 테스트 통과 시                                       │
│  ┌─────────────────┐                                            │
│  │  Build Stage    │                                            │
│  │  - JAR 빌드     │                                            │
│  │  - 배포 번들 생성│                                            │
│  └─────────────────┘                                            │
│           ↓                                                     │
│  ┌─────────────────┐                                            │
│  │   AWS S3 업로드  │                                            │
│  └─────────────────┘                                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      AWS CodeDeploy                             │
├─────────────────────────────────────────────────────────────────┤
│  S3에서 번들 다운로드 → EC2로 배포                               │
│                                                                 │
│  배포 파일:                                                      │
│  - build/libs/*.jar                                             │
│  - Dockerfile                                                   │
│  - docker-compose.yml                                           │
│  - appspec.yml                                                  │
│  - scripts/deploy.sh                                            │
│  - .env                                                         │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        AWS EC2                                  │
├─────────────────────────────────────────────────────────────────┤
│  deploy.sh 실행                                                  │
│           ↓                                                     │
│  ┌─────────────────────────────────────┐                        │
│  │         Docker Compose              │                        │
│  │  ┌─────────────┐  ┌─────────────┐   │                        │
│  │  │   MySQL     │  │ Spring Boot │   │                        │
│  │  │ Container   │  │ Container   │   │                        │
│  │  │             │  │             │   │                        │
│  │  │   Redis     │  │ Elasticsearch│  │                        │
│  │  └─────────────┘  └─────────────┘   │                        │
│  └─────────────────────────────────────┘                        │
└─────────────────────────────────────────────────────────────────┘
```

### 주요 파일

| 파일 | 역할 |
|------|------|
| .github/workflows/gradle.yml | CI/CD 파이프라인 정의 |
| Dockerfile | Spring Boot 앱 컨테이너 이미지 |
| docker-compose.yml | MySQL + Redis + Elasticsearch + Spring Boot 컨테이너 구성 |
| appspec.yml | CodeDeploy 배포 설정 |
| scripts/deploy.sh | EC2에서 Docker Compose 실행 |
| scripts/validate.sh | 배포 후 Health Check 검증 |
| .env | 애플리케이션 환경변수 설정 파일 |

---

## 10. 트러블슈팅

### 1. N+1 쿼리로 인한 성능 저하

**문제 상황**
- 게시글 100개 조회 시 101개의 쿼리 발생

**원인**
- JPA 지연 로딩(LAZY)으로 연관 엔티티 접근 시마다 추가 쿼리 발생

**해결 방법**
- `LEFT JOIN FETCH`로 연관 엔티티 즉시 로딩
- 페이징 시 `countQuery` 분리

---

### 2. JWT 토큰 탈취 시 보안 취약점

**문제 상황**
- 로그아웃 후에도 탈취된 토큰으로 API 접근 가능

**원인**
- JWT는 Stateless하여 서버에서 무효화 불가

**해결 방법**
- Redis 기반 토큰 블랙리스트 구현
- Refresh Token Rotation으로 토큰 재사용 방지

---

### 3. S3 이미지 삭제로 인한 응답 지연

**문제 상황**
- 게시글 삭제 시 S3 이미지 삭제 대기로 2.5초 지연

**원인**
- 동기 처리로 S3 API 응답 대기

**해결 방법**
- `@Async`로 비동기 처리
- 메인 트랜잭션과 분리하여 S3 실패 시에도 게시글 삭제 성공

---

### 4. SSE 다중 인스턴스 문제

**원인**
- 인스턴스 간 메모리 공유 불가

**해결**
- Redis Pub/Sub으로 이벤트 브로드캐스트

---

### 5. HTTPS 적용 문제

**원인**
- SSL 인증서 미적용

**해결**
- Nginx + Route 53 + ACM 설정 후 HTTPS 전환

---

### 6. 알림 발송으로 인한 응답 지연 및 알림 유실

**문제 상황**
- 멘토 게시글 작성 시 팔로워 수에 비례해 응답이 느려짐 (팔로워 10명 기준 약 2.1초 예상)
- 수신자가 접속 중이 아니면 알림이 그대로 사라짐

**원인**
- 팔로워 루프 안에서 FCM 외부 API를 동기 호출하여 왕복 시간이 누적
- Email은 SMTP 연결 + 인라인 이미지 첨부로 단건 1초 이상 소요
- SSE Emitter가 없을 때의 처리 경로 부재

**해결 방법**
- 알림 전용 스레드풀(`notificationExecutor`)로 요청 스레드와 분리
- 이벤트 발행 기반으로 전환하여 발송 호출은 즉시 반환
- 실패 · 미접속 건은 재시도 큐에 적재하고 지수 백오프(2초 → 최대 60초)로 재전송
- 채널별 예외 격리로 한 채널 실패가 전체 발송을 중단시키지 않도록 처리

---

## 11. 실행 방법

### 1. 인프라 실행

```bash
docker network create toty-network

docker run -d --name toty-mysql \
  --network toty-network \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=test \
  -e MYSQL_DATABASE=toty_dev \
  mysql:8.0

docker run -d --name toty-redis \
  --network toty-network \
  -p 6379:6379 \
  redis:7-alpine

docker run -d --name toty-elasticsearch \
  --network toty-network \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e ES_JAVA_OPTS="-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.1

docker build -t toty-app .

docker run -d --name toty-app \
  --network toty-network \
  -p 8070:8070 \
  --env-file .env \
  toty-app
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3. Swagger

```
http://localhost:8070/swagger-ui
```

---

## 프로젝트를 통해 얻은 경험

- 실시간 시스템 설계 경험 (WebSocket, SSE, Redis Pub/Sub)
- JWT + OAuth2 인증 직접 구현
- 다채널 알림 시스템 설계 (SSE, FCM, Email, SMS)
- Redis 캐시 전략 설계
- N+1 문제 해결 및 쿼리 최적화
- DDD 패턴 적용 (Value Object, Domain Event, Strategy/Factory)
- ElasticSearch 도입 및 검색 고도화 경험
- HTTPS 전환 및 도메인 연결 경험
- 테스트 인프라 구축 (단위/통합/E2E 분리)

---

## 담당 영역

### 팀 프로젝트 당시
- 게시글/댓글 도메인 설계 및 구현
- 멘토 선정 날짜 스케줄 설정
- Redis 기반 SSE 구축
- FCM 알림 시스템 구축
- 멘토 선정 & 박탈 이메일/SMS 발송 구현

### 개인 고도화
- ElasticSearch 도입
- 캐시 전략 개선
- 테스트 코드 보강
- 구조 리팩토링
- Nginx 기반 HTTPS 전환
- 가비아 도메인 + Route 53 + ACM 연동
- JWT 보안 강화 (블랙리스트, Token Rotation)
- N+1 쿼리 최적화

---
