# TOTY (멘토링 기반 개발자 커뮤니티 & 알림 아키텍처)

멘토-멘티를 연결하는 지식 공유 커뮤니티 및 대규모 확장성을 고려한 비동기 알림 시스템 백엔드

## 1. 프로젝트 개요

### 1-1. **기획 배경 및 프로젝트 진화**

- **Phase 1. 팀 프로젝트 (MVP & 알림 기반 구축):** 인스타그램식 팔로우 구조 기반의 멘토링 커뮤니티 서비스를 구축했습니다. 게시글/댓글 도메인과 알림 아키텍처를 전담하여, SOLID 원칙 및 Strategy/Factory 패턴 기반의 게시글·알림 구조 설계와 Redis Pub/Sub을 통한 다중 서버 SSE 세션 공유 대비 체계를 마련했습니다.
- **전환 계기 (The Turning Point):** 서비스 구축 후 진행한 성능 및 스트레스 테스트에서 알림 발송 시 외부 API(FCM/Email/SMS) 응답 지연이 사용자 액션을 블로킹하고 SSE Timeout을 유발하는 현상, 팔로우/프로필/게시글 조회 시 반복되는 DB I/O 병목 및 N+1 문제, S3 파일 업로드/삭제 시 스레드 점유 지연 등 구조적 한계를 확인했습니다.
- **Phase 2. 개인 고도화 (응답성, 보안 & 시스템 정합성 고도화):** 체감한 병목을 극복하기 위해 @Async 기반 전용 스레드풀 분리, Redis Caching, QueryDSL fetchJoin() 최적화, JWT RTR & Redis 블랙리스트 구축, Spring Event 기반 캐시 정합성 확보, Nginx 리버스 프록시 기반 HTTPS 보안 통신 환경을 구축하여 시스템 안정성과 응답 속도를 극대화했습니다.

### 1-2. 프로젝트 목표

- Strategy/Factory 패턴을 적용해 게시글 카테고리 확장 및 알림 전송 매체 추가 시 기존 코드 변경 없는 개방-폐쇄 원칙(OCP) 준수
- 외부 서비스 호출(FCM, Email, S3) 및 알림 전송을 비동기 분리하여 메인 트랜잭션의 지연 및 SSE Timeout 최소화
- QueryDSL fetchJoin() 및 Redis Caching을 통한 RDB I/O 병목 완화 및 API 응답 속도 극대화
- Redis Pub/Sub 도입으로 Scale-out 환경에서도 SSE 알림 유실 없는 브로드캐스팅 확장성 보장
- Nginx + SSL 적용 및 자동화된 CI/CD 배포 파이프라인 구축

### 1-3. 기술 스택

- **Core:** Java 17, Spring Boot 3.x, Spring Data JPA, QueryDSL
- **Security & Auth:** Spring Security, JWT (RTR & Redis Blacklist), OAuth2
- **Notification & Messaging:** SSE (Server-Sent Events), Firebase FCM, Redis Pub/Sub, CoolSMS, Gmail SMTP
- **Database & Search:** MySQL 8.0, Redis, Elasticsearch
- **DevOps & Testing:** Docker, Nginx, GitHub Actions, AWS CodeDeploy, AWS EC2, Route 53, AWS ACM
- **Testing:** JUnit5, Mockito, AssertJ, Testcontainers
- **Docs**: Swagger

### 1-4. 시스템 아키텍처 및 CI/CD 파이프라인

**[시스템 아키텍처]**

```
                      [ Users & Clients ]
                              │
               (HTTPS / SSE / WebSocket / FCM Push)
                              │
     ┌────────────────────────┴────────────────────────┐
     ▼                                                 ▼
[ Nginx Container ]                          [ FCM (Google Server) ]
 (Reverse Proxy / SSL)                        (External Push Service)
     │                                                 ▲
     ▼                                                 │ (Async Push Call)
[ Spring Boot Application (Docker Container) ] ────────┘
┌────────────────────────────────────────────────────────┐
│  - Core API / Auth / Community Logic                   │
│  - Async Notification Engine (Strategy/Factory)        │
│  - QueryDSL / Event-driven Cache Manager               │
└──────┬──────────────┬──────────────┬──────────┬────────┘
       │              │              │          │
       ▼              ▼              ▼          ▼
  [ MySQL ]      [ Redis ]     [ Elastic-  [ RabbitMQ ]
 (Primary DB)  (PubSub/Cache)    search ]   (Chat Broker)

```

**[CI/CD 배포 파이프라인]**

```
GitHub Push (main/develop)
  └─► GitHub Actions
       ├─► JUnit5 / Mockito 통합 테스트
       ├─► Gradle Build & Docker Image 빌드 / Hub Push
       └─► AWS S3 업로드 ──► AWS CodeDeploy ──► EC2 (Nginx + Docker Compose 배포)

```

## 2. 도메인 및 구조 설계

### 2-1. 패키지 및 프로젝트 구조

```
com.toty
├── common               # Security/JWT, Async ThreadPool, Redis PubSub 설정 및 Global Error
└── domain
    ├── user             # 사용자 프로필, OAuth2 및 `@Cacheable("userInfo")` Redis 캐싱
    ├── following        # 멘토-멘티 팔로우 관계 관리
    ├── post             # 게시글 카테고리별 Strategy+Factory 패턴, QueryDSL fetchJoin() 및 Elasticsearch 검색
    ├── comment          # 댓글/대댓글 계층 구조 관리
    ├── chat             # 실시간 커뮤니티 채팅 메시징 (WebSocket)
    ├── notification     # 알림 비동기 이벤트, Strategy/Factory 구현체, NotificationRetryQueue, Redis Pub/Sub
    └── roleRefreshScheduler  # Spring Scheduler 기반 사용자 권한/역할 자동 갱신

```

## 3. 핵심 기능 및 담당 도메인

### 3-1. 핵심 기능 요약

- **멘토링 커뮤니티:** 게시글(지식/일반/질문) 카테고리별 작성, 댓글/대댓글 계층 구조, 멘토 선정 및 권한 자동 전환
- **멘토 선정 & 박탈:** Spring Scheduler 기반 멘토 권한 자동 부여/박탈, 채팅방 접근 권한 자동 갱신 및 다중 채널 알림 연동
- **멘토 중심 채팅방:** WebSocket + RabbitMQ 기반 실시간 통신, 멘토/선정 멘티 검증 인가, 메시지 저장 및 오프라인 푸시 연동
- **실시간 비동기 다중 알림:** SSE 기반 실시간 알림, FCM 푸시, SMS, Email 채널별 차등 발송
- **Elasticsearch 검색:** 게시글 제목/본문 고성능 키워드 검색

### 3-2. 핵심 비즈니스 로직

| 구분               | 비즈니스 규칙 및 지표                                       | 처리 방식                                                    |
| ------------------ | ----------------------------------------------------------- | ------------------------------------------------------------ |
| **알림 확장성**    | 전송 채널(SSE, FCM, SMS, Email) 추가 시 기존 코드 수정 금지 | NotificationStrategy 인터페이스 + NotificationFactory 패턴 적용 |
| **다중 서버 알림** | Scale-out 시 특정 인스턴스에 종속된 SSE 세션 유실 방지      | Redis Pub/Sub 메시지 브로드캐스팅으로 해당 세션 보유 서버만 즉시 전송 |
| **인증 객체 접근** | Controller마다 반복되는 SecurityContextHolder 호출 제거     | ArgumentResolver 기반 @CurrentUser 커스텀 어노테이션 구축 |

### 3-3. 담당 영역 및 역할

| 구분        | 기간 및 형태                     | 역할 및 주요 담당 도메인                                 |
| ----------- | -------------------------------- | -------------------------------------------------------- |
| **Phase 1** | 2024.12 \~ 2025.02 (팀 5명)      | **Backend Developer (게시글·댓글 & 알림 아키텍처 전담)** |
| Phase 2     | 2025.10 \~ 2026.01 (개인 고도화) | **성능 최적화 및 프로덕션 인프라 고도화**                    |

**Phase 1 주요 담당**

- Strategy + Factory 패턴 기반 카테고리별 게시글·댓글 도메인 설계
- Spring Scheduler 기반 멘토 권한 자동 전환 로직 구현
- SSE, FCM, Email, SMS 다중 비동기 알림 파이프라인 수립
- Redis Pub/Sub 기반 분산 환경 알림 브로드캐스트 구현 

**Phase 2 주요 담당**

- @Async 비동기화 적용으로 알림/파일 처리 지연 및 SSE Timeout 해결
- QueryDSL fetchJoin() 적용으로 JPA N+1 쿼리 최적화
- Redis 캐싱 및 Spring Event 기반 캐시 정합성 무효화 설계
- JWT RTR + Redis 블랙리스트 보안 체계 및 Nginx SSL / CI/CD 구축 |

## **4. 엔지니어링 문제 해결 및 회고**

**■ [Phase 전환 배경] 팀 프로젝트 완료 후 인식한 구조적 한계와 고도화 명분**

- **외부 API 동기 블로킹:** 알림 전송, S3 이미지 삭제 등 외부 I/O 작업이 메인 트랜잭션에 동기로 묶여 외부 API 지연 및 장애 발생 시 전체 응답이 대기되는 병목 현상 확인.
- **과도한 DB I/O 및 CPU 점유:** 게시글 N+1 쿼리 및 프로필 반복 조회가 매 요청마다 RDB(MySQL) 디스크 조회로 이어져 서버 CPU 점유율 및 Latency 상승.
- **환경 불일치 리스크:** 로컬 테스트 환경과 실제 운영 환경(MySQL, Redis, S3)의 미세한 기능/인덱스 차이로 배포 전 통합 테스트의 신뢰도 확보 필요성 대두.

### **4-1. 성능 개선 및 구조 최적화**

| 개선 항목                   | 도입 과정                                                    | 내용                                                         | 성과                                                         |
| --------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **S3 이미지 삭제 비동기화** | 프로필 이미지 변경/삭제 시 S3 삭제 API I/O 동기 블로킹 현상 개선 | @Async 기반 전용 스레드풀 분리 및 백그라운드 비동기 삭제 파이프라인 전환 | 프로필 수정 API 응답 속도 **2.5초 ➔ 0.5초** 단축 (80% 개선)  |
| **JPQL Fetch Join 최적화**  | 게시글 목록, 팔로우, 좋아요 조회 시 발생하던 N+1 쿼리 차단   | Post, Following, PostLike 연관관계 대상 Fetch Join 적용 | N+1 쿼리 발생 원인 차단 및 **Single Query 전환** (450ms ➔ 32ms) |
| **Redis 프로필 캐싱**       | 반복적인 프로필 DB 조회 병목 해소를 위한 Caching 적용        | @Cacheable("userInfo") 적용을 통한 Redis 캐시 적재         | DB 프로필 조회 쿼리 수 **70% 절감**                          |
| **JWT 보안 체계 강화**      | 단일 토큰 방식의 탈취 위험 및 로그아웃 토큰 재사용 방지      | Token Rotation(RTR) 도입 및 Redis 기반 로그아웃 블랙리스트 구축 | 토큰 탈취 위험 최소화 및 보안성 극대화                       |

### 4-2. Phase 1 트러블슈팅 (MVP 팀 프로젝트 단계)

- **동기식 알림 호출로 인한 SSE Timeout 및 서비스 응답 지연** 
  - **문제 상황:** 서비스 초기에는 게시글 작성이나 댓글 등록 시 SSE(Server-Sent Events), FCM 웹푸시, Email, SMS 등 멀티 채널 알림을 동기 방식(Synchronous)으로 발송했습니다. 이로 인해 알림 제공자(FCM/Email/SMS)의 외부 API 응답이 조금만 지연되거나, SSE 연결을 유지하는 과정에서 대기 시간이 발생하면 사용자가 게시글을 작성한 후 화면이 넘어가지 않고 응답이 지연되거나 SSE Timeout이 빈번하게 발생하는 문제가 나타났습니다.
  - **원인 분석:** 메인 비즈니스 로직을 처리하는 HTTP 요청 스레드가 알림 전송이라는 외부 네트워크 I/O 작업이 완료될 때까지 계속 블로킹(Blocking)되었기 때문입니다. 즉, 알림 전송 실패나 지연이 핵심 비즈니스 로직의 성능에 직접적인 영향을 주고 있었습니다.
  - **해결 방법:** Spring의 ApplicationEventPublisher를 도입하여 "댓글이 작성됨"이라는 도메인 이벤트만 발행하고, 실제 알림 발송 로직은 이벤트를 구독하여 처리하도록 **발행/구독(Pub/Sub) 아키텍처로 분리**했습니다. 또한 알림 처리 메서드에 @Async 어노테이션과 전용 스레드풀(notificationExecutor)을 지정하여, 알림 발송 작업이 백그라운드 스레드에서 완전히 독립적으로 동작하도록 전환했습니다.
  - **최종 결과:** 핵심 비즈니스 로직은 알림 전송 완료를 기다리지 않고 클라이언트에게 즉시 성공 응답을 반환하게 되었습니다. 이로 인해 사용자 체감 응답 속도가 크게 향상되었으며, 외부 알림 API에 장애가 발생하더라도 메인 서비스는 영향을 받지 않는 **시스템 간 결합도 저하 및 독립성 확보**를 달성했습니다.
- **외부 API 발송 실패에 따른 알림 유실 및 장애 격리** 
  - **문제 상황:** Email SMTP 서비스나 FCM 서버 등 외부 알림 채널의 일시적인 네트워크 지연이나 서비스 점검이 발생할 경우, 유저에게 전달되어야 할 알림이 즉시 실패 처리되어 유실되는 현상이 발생했습니다.
  - **원인 분석:** 외부 네트워크 상태는 언제든 불안정해질 수 있음에도 불구하고, 발송 실패 시 이를 재시도하거나 예외 상황을 안전하게 흡수할 수 있는 재처리 메커니즘이 부재했기 때문입니다.
  - **해결 방법:** 각 알림 채널(SSE, FCM, Email, SMS)을 독립된 예외 격리 구조로 설계하여 특정 채널이 실패해도 타 채널 발송에 영향을 주지 않도록 정비했습니다. 그리고 발송에 실패한 알림은 즉시 버리지 않고, **지수 백오프(Exponential Backoff: 초기 2초대에서 최대 60초까지 대기 시간을 2배씩 늘려가며 최대 5회 재시도)** 알고리즘이 적용된 NotificationRetryQueue에 적재하여 백그라운드에서 주기적으로 재발송을 시도하도록 구현했습니다. 만약 5회 재시도 후에도 최종 실패할 경우, DB(NotificationFailLog)에 실패 이력을 저장하도록 조치했습니다.
  - **최종 결과:** 외부 API의 일시적 장애 상황에서도 알림 유실률을 크게 낮췄으며, 최종 실패 이력을 DB에 남겨 추후 데이터 추적 및 재처리 작업이 가능해짐으로써 **시스템의 알림 수신 안정성과 데이터 최종 정합성**을 극대화했습니다.
- **다중 서버(Scale-out) 환경의 SSE 세션 유실 해결** 
  - **문제 상황:** 서버 인스턴스를 확장(Scale-out)할 경우, 유저가 접속한 서버와 알림 이벤트가 발생한 서버가 달라 SSE 알림이 정상적으로 도달하지 않고 유실되는 문제 발생.
  - **원인 분석:** SSE 세션 객체는 서버 메모리(In-Memory)에 존재하므로 타 인스턴스로 직접 전파되지 않음.
  - **해결 방법:** Redis Pub/Sub을 중앙 메시지 브로커로 도입. 알림 발생 시 Redis 채널로 이벤트를 발행하고, 모든 서버 인스턴스가 이를 수신하여 자신의 로컬 메모리에 해당 유저의 SSE 세션이 있는 경우에만 알림을 전송하는 구조로 구현.
  - **최종 결과:** 이벤트 기반 아키텍처(EDA) 완성 및 분산 환경에서도 알림 도달률 100% 보장.

### 4-3. Phase 2 성능 최적화 (개인 리팩토링 및 고도화 단계)

- **S3 이미지 삭제 비동기화 (**@Async**)** 
  - **도입 배경:** 유저가 프로필 이미지를 변경하거나 기존 이미지를 삭제할 때, AWS S3 스토리지에 존재하는 실제 객체를 삭제하는 네트워크 I/O 작업이 동기식 블로킹(Synchronous Blocking) 방식으로 처리되고 있었습니다. 이로 인해 프로필 수정 버튼을 누른 후 S3 서버의 삭제 응답을 받을 때까지 유저의 브라우저가 대기 상태에 빠지는 성능 병목이 발생했습니다.
  - **개선 내용:** 프로필 데이터베이스 수정 작업과 S3 파일 삭제 작업을 완전 분리했습니다. DB상의 유저 정보 업데이트를 먼저 완료해 클라이언트에 즉시 응답을 돌려준 뒤, S3 객체 삭제 작업은 @Async 기반의 별도 전용 스레드풀(taskExecutor)을 활용해 백그라운드에서 비동기로 처리되도록 파이프라인을 구축했습니다.
  - **성능 성과:** 프로필 수정 API의 전체 응답 속도를 기존 **2.5초에서 0.5초로 약 80% 단축**하여, 파일 업로드/삭제 I/O로 인한 사용자 대기 시간을 대폭 줄였습니다.
- **QueryDSL** **fetchJoin()** **도입을 통한 JPA N+1 쿼리 최적화** 
  - **도입 배경:** 게시글 목록 조회, 팔로우 목록 조회, 게시글 좋아요 확인 등 연관된 엔티티(Post, Following, PostLike 등)를 함께 불러와 화면에 뿌려주는 주요 API를 테스트하던 중, 게시글 1건을 조회할 때 연관된 유저나 좋아요 정보를 가져오기 위해 십수 개 이상의 추가 SQL 쿼리가 연속적으로 실행되는 N+1 문제를 발견했습니다. AI 도구를 활용해 실행되는 SQL 로그와 실행 흐름을 분석하며, JPA의 지연 로딩(Lazy Loading)으로 인해 N+1 문제가 발생하고 있음을 정확히 인지했습니다.
  - **개선 내용:** 단순 JPA Repository의 기본 메서드 대신 복잡한 동적 쿼리와 최적화에 유리한 QueryDSL을 도입했습니다. 연관된 엔티티들을 데이터베이스 단에서 **단 1회의 SQL** **JOIN** **문으로 한 번에 묶어서 끌어오는** **fetchJoin()** **기법**을 적용했습니다.
  - **성능 성과:** 게시글 메인 목록 조회 시 기존에 41회에 달하던 반복적인 DB SQL 실행 횟수를 **단 1회로 단축**시켰으며, 그 결과 API 응답 속도를 기존 **450ms에서 32ms로 약 97% 대폭 개선**하는 극적인 성과를 거두었습니다.
- **Redis 프로필 캐싱 (**@Cacheable**)** 
  - **도입 배경:** 사용자 프로필 정보 및 팔로우 상태 조회는 서비스 내에서 가장 자주 호출되는 Read-heavy(조회 중심) 요청입니다. 사용자가 커뮤니티 활동을 할 때마다 매번 RDB(MySQL)로 직접 조회 쿼리가 전송되어, 서비스 사용자가 늘어날수록 DB 디스크 I/O와 CPU에 큰 부하가 집중되는 구조적인 한계가 있었습니다.
  - **개선 내용:** Spring Cache Abstraction 기능을 활용하여 @Cacheable("userInfo") 어노테이션을 적용했습니다. 자주 조회되는 유저 프로필 데이터를 인메모리(In-Memory) 데이터베이스인 Redis에 Key-Value 형태로 캐싱하여, 이후 동일한 프로필 조회 요청이 들어오면 DB를 거치지 않고 Redis에서 즉시 응답을 반환하도록 구조를 개선했습니다.
  - **성능 성과:** 반복적인 RDB 프로필 조회 접근을 인메모리 레이어에서 흡수함으로써 **DB 프로필 조회 쿼리 발생 빈도를 70% 이상 절감**시켰고, DB 부하를 크게 완화했습니다.
- **JWT 보안 체계 강화 (RTR & Redis 블랙리스트)** 
  - **도입 배경:** 단일 Access Token만 사용하는 방식은 토큰의 유효기간이 길 경우 탈취 시 보안에 매우 취약하며, 사용자가 로그아웃을 수행하더라도 이미 발급된 JWT 토큰 자체는 만료 시간이 지나기 전까지 서버에서 강제로 무효화할 수 없는 보안적 허점이 있었습니다.
  - **개선 내용:** Refresh Token을 이용하여 Access Token을 재발급받을 때마다 Refresh Token까지 함께 새롭게 재발급하여 기존 토큰을 무효화하는 **RTR(Refresh Token Rotation)** 기법을 구현했습니다. 또한 사용자가 로그아웃을 요청할 경우, 해당 Access Token의 남은 유효시간을 계산하여 Redis에 블랙리스트(Blacklist)로 등록하고, API 요청이 들어올 때마다 Security Filter 단에서 Redis 블랙리스트 여부를 검증하도록 구축했습니다.
  - **성능 성과:** 탈취된 토큰의 재사용 가능 기간을 극도로 단축시켰을 뿐만 아니라, 로그아웃된 토큰으로 들어오는 무단 접근을 중앙 서버에서 즉시 차단함으로써 보안 체계의 완전성을 높였습니다.

### 4-4. Phase 2 기술적 도전 (캐시 정합성 설계)

#### **다변화된 프로필 수정 경로에서의** **@CacheEvict** **범위 설계 및 정합성 문제 해결**

- **문제 상황:** Redis 프로필 캐싱 도입 후, 사용자가 자신의 프로필을 수정했음에도 불구하고 마이페이지나 게시글에서 이전의 오래된 프로필 정보가 그대로 조회되는 **Stale Cache(데이터 불일치 및 오래된 캐시 잔존)** 현상이 발생했습니다.
- **원인 분석:** 기본 프로필 정보 수정 API(updateUserBasicInfo)에는 캐시를 삭제하는 @CacheEvict가 잘 설정되어 있었으나, 프로젝트 내에는 프로필 이미지 변경, 멘토링 상태 변경, 비밀번호 변경 등 유저 엔티티의 상태를 업데이트하는 로직이 여러 도메인 서비스 메서드에 분산되어 있었습니다. 특정 수정 서비스 경로에서 @CacheEvict 지정을 누락하여 Redis의 기존 캐시가 만료(TTL)되기 전까지 무효화되지 않고 남아있었던 것이 원인이었습니다.
- **해결 방법:** 
  1. 유저 상태 변경을 일으키는 프로젝트 내 모든 서비스 메서드를 전수 조사하여 Caching Target Key(userInfo::#userId)의 무효화 타깃 범위를 정밀하게 재설계했습니다.
  2. 추후 새로운 수정 기능이 추가될 때 개발자의 실수로 어노테이션 지정을 누락하는 문제를 근본적으로 방지하기 위해, Spring Domain Event 기반의 캐시 무효화 구조를 구현했습니다. 유저 엔티티의 변경 이벤트가 발생하면 이벤트를 감지하여 자동으로 @CacheEvict 로직이 동작하도록 설계하여 결합도를 낮추고 안전성을 확보했습니다.
- **최종 결과:** 프로필 정보가 변경되는 모든 업데이트 경로에서 캐시 정합성(Consistency) 100%를 보장하게 되었으며, 수동 무효화 누락으로 인한 데이터 불일치 문제를 구조적으로 완벽히 해결했습니다.

### 4-5. 시스템 한계 인지 및 확장성

- **MySQL ↔ Elasticsearch 식별자(ID) 불일치로 인한 정합성 한계 (가장 치명적)** 
  - **문제 상황:** PostSearchService.savePost() 코드 분석 중, 게시글 검색을 위해 Elasticsearch에 문서를 저장할 때 ES Document ID를 RDB의 PK 값이 아닌 UUID.randomUUID()로 임의 생성하고 있음을 발견했습니다 (// TODO: mysql의 pk 값이 저장되어야 함 기술 부채 주석 잔존).
  - **한계점:** RDB(MySQL)의 Auto-increment PK와 ES의 Document ID가 서로 다르게 매핑되어 있기 때문에, 사용자가 게시글을 수정하거나 삭제했을 때 Elasticsearch 인덱스 내의 특정 문서를 정확히 찾아내어 업데이트하거나 삭제하는 데이터 동기화 파이프라인을 정상 작동시키기 어려운 정합성 한계가 존재합니다.
  - **개선 방향:** ES 인덱싱 시 MySQL의 PK 값을 Document ID로 명시적 매핑하도록 수정해야 합니다. 나아가 DB 트랜잭션이 성공적으로 완료된 직후에만 ES 이벤트를 발행하도록 @TransactionalEventListener(AFTER_COMMIT)를 적용하고, 만약 ES 저장에 실패할 경우 재시도할 수 있는 보상 트랜잭션 파이프라인을 구축할 계획입니다.
- **RoleRefreshScheduler** **전체 유저 메모리 로드 및 N+1 쿼리 폭증 위험** 
  - **문제 인지:** 매월 1일 자정 스케줄러가 실행되어 멘토/멘티 권한을 자동 갱신할 때, userRepository.findAllByIsDeletedFalse()를 호출해 DB 내 탈퇴하지 않은 모든 유저 목록을 한 번에 애플리케이션 메모리(RAM)로 끌어오도록 구현되어 있습니다. 그 후 반복문을 돌며 각 유저별로 followingService.countFollowers(id) 쿼리를 개별적으로 실행하고 있습니다.
  - **한계점:** 서비스의 유저 수가 증가할 경우 전체 유저 객체가 한 번에 메모리에 로드되면서 **OOM(Out Of Memory) 장애**를 유발할 위험이 매우 높습니다. 또한 유저 수 $N$명에 비례해 팔로워 수를 조회하는 쿼리가 $N$번 추가 실행되는 폭증(N+1) 부하 구조를 가지고 있습니다.
  - **개선 방향:** 대용량 데이터 처리에 적합한 Spring Batch를 도입하여 유저 데이터를 Chunk 단위(예: 100건/1,000건씩 끊어 읽기)로 Paging 처리하여 메모리 점유율을 일정하게 유지해야 합니다. 또한 팔로워 수 조회 로직은 단일 `GROUP BY` 집계 쿼리로 일괄 조회하도록 쿼리를 최적화할 계획입니다.
- **ConcurrentLinkedQueue 기반 In-Memory Retry Queue의 휘발성 한계** 
  - **문제 인지:** 알림 전송 실패 시 지수 백오프 재시도를 담당하는 NotificationRetryQueue가 서버 내부 자바 메모리 객체인 ConcurrentLinkedQueue로 구현되어 있습니다.
  - **한계점:** SSE 다중 서버 세션 공유는 Redis Pub/Sub을 사용해 분산 환경을 고려했으나, 정작 알림 재시도 큐는 단일 서버 메모리에 남아있는 구조적 불일치가 존재합니다. 이로 인해 알림 재시도 대기 중에 서버가 재시작되거나 배포되면 메모리에 남아있던 재시도 알림 데이터가 전량 유실되며, Scale-out(다중 서버) 환경에서 특정 서버 인스턴스에만 재처리 작업이 귀속되는 한계가 있습니다.
  - **개선 방향:** In-Memory 큐를 제거하고, 데이터 영속성(Persistence)이 보장되는 외부 메시지 브로커인 Redis Stream, RabbitMQ, 또는 Kafka를 도입하여 서버가 다운되더라도 메시지 유실 없이 최소 한 번은 전송을 보장하는 **At-least-once (최소 한 번 전달)** 아키텍처로 진화시킬 예정입니다.

## 5. 테스트 전략

- **결합 검증 및 단위 테스트:** JUnit5 + Mockito 기반의 단위 테스트와 함께, 실제 MySQL 및 H2 환경을 활용한 통합 테스트 수행.
- **검증 독립성 확보:** @ActiveProfiles("test") 환경에서 데이터베이스 및 캐시 환경을 분리하여 알림 이벤트 발행, @Cacheable("userInfo") 적재, 지수 백오프 Retry 로직의 정합성 검증.

## 6. 실행 방법 (Local Run)

Bash

```
# 1. Repository Clone
$ git clone <https://github.com/HyunRe/TOTY.git>
$ cd TOTY

# 2. Infra Containers Run (MySQL, Redis, Elasticsearch)
$ docker-compose up -d

# 3. Application Run
$ ./gradlew bootRun

```
