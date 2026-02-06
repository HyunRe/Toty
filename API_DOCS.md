# Toty API 명세서

> **작성일**: 2026-01-09
> **버전**: 1.0.0
> **Base URL**: `https://toty.cloud`

## 목차
- [1. User API](#1-user-api)
- [2. Post API](#2-post-api)
- [3. Post Search API](#3-post-search-api-elasticsearch)
- [4. Comment API](#4-comment-api)
- [5. Follow API](#5-follow-api)
- [6. Chat API](#6-chat-api)
- [7. Notification API](#7-notification-api)
- [8. User Scrape API](#8-user-scrape-api)
- [9. FCM Token API](#9-fcm-token-api)
- [10. SMS API](#10-sms-api)
- [11. Image API](#11-image-api)
- [12. SSE API](#12-sse-server-sent-events-api)

---

## 1. User API

### Base URL: `/api/users`

#### 1.1 테스트 엔드포인트
- **Method**: `GET`
- **Path**: `/test`
- **Request**: 없음 (인증 필요)
- **Response**: `User`
- **설명**: 현재 로그인한 사용자 정보를 반환합니다 (테스트용)

#### 1.2 이메일 중복 확인
- **Method**: `GET`
- **Path**: `/check-email`
- **Request Parameters**:
  - `email` (String, required)
- **Response**: `SuccessResponse<String>`
- **설명**: 회원가입 시 이메일 사용 가능 여부를 확인합니다

#### 1.3 닉네임 중복 확인
- **Method**: `GET`
- **Path**: `/check-nickname`
- **Request Parameters**:
  - `nickname` (String, required)
- **Response**: `SuccessResponse<String>`
- **설명**: 회원가입 시 닉네임 사용 가능 여부를 확인합니다

#### 1.4 휴대폰 인증번호 요청
- **Method**: `POST`
- **Path**: `/authCode`
- **Request Parameters**:
  - `phoneNumber` (String, required)
- **Response**: `SuccessResponse<Void>`
- **설명**: 회원가입 시 휴대폰 번호로 인증번호를 전송합니다

#### 1.5 휴대폰 인증번호 확인
- **Method**: `POST`
- **Path**: `/check-authCode`
- **Request Parameters**:
  - `authCode` (String, required)
  - `phoneNumber` (String, required)
- **Response**: `SuccessResponse<Boolean>`
- **설명**: 전송된 인증번호의 유효성을 확인합니다

#### 1.6 회원 탈퇴
- **Method**: `DELETE`
- **Path**: `/`
- **Request**: 없음 (인증 필요)
- **Response**: `SuccessResponse<Void>`
- **설명**: 현재 로그인한 사용자의 계정을 탈퇴 처리하고 쿠키를 삭제합니다

#### 1.7 기본 정보 수정
- **Method**: `POST`
- **Path**: `/update`
- **Request Parts**:
  - `basicInfo` (BasicInfoUpdateRequest, required)
  - `profileImage` (MultipartFile, optional)
- **Response**: `SuccessResponse<Void>`
- **설명**: 닉네임과 프로필 사진을 수정합니다

#### 1.8 내 링크 정보 조회
- **Method**: `GET`
- **Path**: `/updateLink`
- **Request**: 없음 (인증 필요)
- **Response**: `ResponseEntity` (링크 정보)
- **설명**: 현재 등록된 링크 정보를 조회합니다

#### 1.9 링크 수정
- **Method**: `PUT`
- **Path**: `/links`
- **Request Body**: `LinkUpdateDto`
- **Response**: `SuccessResponse<Void>`
- **설명**: 사용자의 링크 정보를 수정합니다

#### 1.10 내 태그 정보 조회
- **Method**: `GET`
- **Path**: `/updateTag`
- **Request**: 없음 (인증 필요)
- **Response**: `ResponseEntity` (태그 정보)
- **설명**: 현재 등록된 태그 정보를 조회합니다

#### 1.11 태그 수정
- **Method**: `PUT`
- **Path**: `/tags`
- **Request Body**: `TagUpdateDto`
- **Response**: `SuccessResponse<Void>`
- **설명**: 사용자의 태그 정보를 수정합니다

#### 1.12 전화번호 수정
- **Method**: `POST`
- **Path**: `/phone-number`
- **Request Body**: `PhoneNumberUpdateRequest`
- **Response**: `SuccessResponse<Void>`
- **설명**: 사용자의 전화번호를 수정합니다

#### 1.13 상태메시지 수정
- **Method**: `POST`
- **Path**: `/status-message`
- **Request Body**:
  - `statusMessage` (String)
- **Response**: `SuccessResponse<Void>`
- **설명**: 사용자의 상태메시지를 수정합니다

#### 1.14 내 정보 조회
- **Method**: `GET`
- **Path**: `/me`
- **Request**: 없음 (인증 필요)
- **Response**: `SuccessResponse` (UserInfo)
- **설명**: 현재 로그인한 사용자의 정보를 조회합니다

#### 1.15 다른 사용자 정보 조회
- **Method**: `GET`
- **Path**: `/{id}`
- **Path Variables**:
  - `id` (Long, required)
- **Response**: `SuccessResponse` (UserInfo)
- **설명**: 특정 사용자의 정보를 조회합니다

#### 1.16 이메일 찾기
- **Method**: `POST`
- **Path**: `/find-email`
- **Request Body**: `FindEmailRequest`
  - `username` (String)
  - `phoneNumber` (String)
- **Response**: `SuccessResponse<Map<String, String>>`
- **설명**: 이름과 전화번호로 이메일을 찾습니다

#### 1.17 비밀번호 재설정
- **Method**: `POST`
- **Path**: `/reset-password`
- **Request Body**: `ResetPasswordRequest`
  - `email` (String)
  - `username` (String)
  - `phoneNumber` (String)
  - `newPassword` (String)
- **Response**: `SuccessResponse<Void>`
- **설명**: 이메일, 이름, 전화번호 인증 후 비밀번호를 재설정합니다

---

## 2. Post API

### Base URL: `/api/posts`

#### 2.1 게시글 삭제
- **Method**: `DELETE`
- **Path**: `/{id}`
- **Path Variables**:
  - `id` (Long, required)
- **Response**: `ResponseEntity<String>`
- **설명**: 특정 게시글을 삭제합니다

#### 2.2 게시글 좋아요 토글
- **Method**: `PATCH`
- **Path**: `/{id}/like`
- **Path Variables**:
  - `id` (Long, required)
- **Request Body**: `PostLikeActionRequest`
  - `likeAction` (Boolean/String)
- **Response**: `ResponseEntity<Integer>` (좋아요 수)
- **설명**: 게시글에 좋아요를 추가하거나 취소합니다

#### 2.3 좋아요 상태 조회
- **Method**: `GET`
- **Path**: `/{id}/like-status`
- **Path Variables**:
  - `id` (Long, required)
- **Response**: `ResponseEntity<Boolean>`
- **설명**: 특정 게시글에 대한 로그인한 사용자의 좋아요 여부를 조회합니다

#### 2.4 게시글 스크랩 토글
- **Method**: `PATCH`
- **Path**: `/{id}/scrape`
- **Path Variables**:
  - `id` (Long, required)
- **Request Body**: `PostScrapeRequest`
  - `scrape` (Boolean/String)
- **Response**: `ResponseEntity<String>`
- **설명**: 게시글을 스크랩하거나 스크랩 취소합니다

#### 2.5 스크랩 상태 조회
- **Method**: `GET`
- **Path**: `/{id}/scrape-status`
- **Path Variables**:
  - `id` (Long, required)
- **Response**: `ResponseEntity<Boolean>`
- **설명**: 특정 게시글에 대한 로그인한 사용자의 스크랩 여부를 조회합니다

#### 2.6 게시글 작성
- **Method**: `POST`
- **Path**: `/create`
- **Request Body**: `PostCreateRequest` (Valid)
- **Response**: `ResponseEntity<Post>`
- **설명**: 새로운 게시글을 작성합니다

#### 2.7 게시글 수정
- **Method**: `PATCH`
- **Path**: `/{id}`
- **Path Variables**:
  - `id` (Long, required)
- **Request Body**: `PostUpdateRequest` (Valid)
- **Response**: `ResponseEntity<Post>`
- **설명**: 특정 게시글을 수정합니다

#### 2.8 전체 게시글 목록 조회
- **Method**: `GET`
- **Path**: `/list`
- **Request Parameters**:
  - `page` (int, optional, default: 1)
  - `sort` (String, optional)
- **Response**: `ResponseEntity<PaginationResult>`
- **설명**: 전체 게시글 목록을 페이지네이션으로 조회합니다

#### 2.9 내가 작성한 게시글 목록 조회
- **Method**: `GET`
- **Path**: `/myList`
- **Request Parameters**:
  - `page` (int, optional, default: 1)
  - `postCategory` (String, optional)
- **Response**: `ResponseEntity<PaginationResult>`
- **설명**: 로그인한 사용자가 작성한 게시글 목록을 페이지네이션으로 조회합니다

#### 2.10 카테고리별 게시글 목록 조회
- **Method**: `GET`
- **Path**: `/categoryList`
- **Request Parameters**:
  - `page` (int, optional, default: 1)
  - `postCategory` (String, optional)
- **Response**: `ResponseEntity<PaginationResult>`
- **설명**: 특정 카테고리의 게시글 목록을 페이지네이션으로 조회합니다

#### 2.11 좋아요한 게시글 목록 조회
- **Method**: `GET`
- **Path**: `/myLikeList`
- **Request Parameters**:
  - `page` (int, optional, default: 1)
  - `postCategory` (String, optional, default: "GENERAL")
- **Response**: `ResponseEntity<PaginationResult>`
- **설명**: 로그인한 사용자가 좋아요한 게시글 목록을 페이지네이션으로 조회합니다

#### 2.12 게시글 상세 조회
- **Method**: `GET`
- **Path**: `/{id}/detail`
- **Path Variables**:
  - `id` (Long, required)
- **Request Parameters**:
  - `page` (int, optional, default: 1)
  - `postCategory` (String, optional)
- **Response**: `ResponseEntity<PostDetailResponse>`
- **설명**: 특정 게시글의 상세 정보를 조회하고 조회수를 증가시킵니다

---

## 3. Post Search API (ElasticSearch)

### Base URL: `/api/search/posts`

#### 3.1 통합 게시글 검색
- **Method**: `GET`
- **Path**: `/`
- **Request Parameters**:
  - `keyword` (String, optional, default: "")
  - `field` (SearchField, required) - TITLE, CONTENT, TITLE_AND_CONTENT
  - `size` (int, optional, default: 5)
- **Response**: `TotyResponse<Map<PostCategory, Page<PostEs>>>`
- **설명**: 모든 카테고리(GENERAL, INFORMATION, QNA)에서 게시글을 검색합니다. 검색 시 처음 나오는 화면에 표시

#### 3.2 카테고리별 게시글 검색
- **Method**: `GET`
- **Path**: `/category`
- **Request Parameters**:
  - `keyword` (String, optional, default: "")
  - `field` (SearchField, required)
  - `category` (PostCategory, required) - GENERAL, INFORMATION, QNA
  - `page` (int, required)
  - `size` (int, optional, default: 5)
- **Response**: `TotyResponse<Map<PostCategory, Page<PostEs>>>`
- **설명**: 특정 카테고리에서 게시글을 페이지네이션으로 검색합니다

#### 3.3 게시글 생성 (테스트용)
- **Method**: `POST`
- **Path**: `/create`
- **Request Parameters**:
  - `title` (String, required)
  - `content` (String, required)
  - `category` (PostCategory, required)
- **Response**: `TotyResponse<String>` (postId)
- **설명**: ElasticSearch에 테스트 게시글을 생성합니다 (추후 삭제 예정)

---

## 4. Comment API

### Base URL: `/api/comments`

#### 4.1 댓글 삭제
- **Method**: `DELETE`
- **Path**: `/{id}`
- **Path Variables**:
  - `id` (Long, required)
- **Response**: `ResponseEntity<String>`
- **설명**: 특정 댓글을 삭제합니다

#### 4.2 댓글 작성
- **Method**: `POST`
- **Path**: `/create`
- **Request Parameters**:
  - `postId` (Long, required)
- **Request Body**: `CommentCreateUpdateRequest` (Valid)
- **Response**: `ResponseEntity<CommentDto>`
- **설명**: 특정 게시글에 새로운 댓글을 작성합니다

#### 4.3 댓글 수정
- **Method**: `PATCH`
- **Path**: `/{id}`
- **Path Variables**:
  - `id` (Long, required)
- **Request Body**: `CommentCreateUpdateRequest` (Valid)
- **Response**: `ResponseEntity<CommentDto>`
- **설명**: 특정 댓글을 수정합니다

#### 4.4 댓글 목록 조회
- **Method**: `GET`
- **Path**: `/list`
- **Request Parameters**:
  - `page` (int, optional, default: 1)
  - `postId` (Long, required)
- **Response**: `ResponseEntity<PaginationResult>`
- **설명**: 특정 게시글의 댓글 목록을 페이지네이션으로 조회합니다

#### 4.5 내가 작성한 댓글 목록 조회
- **Method**: `GET`
- **Path**: `/myList`
- **Request Parameters**:
  - `page` (int, optional, default: 1)
- **Response**: `ResponseEntity<PaginationResult>`
- **설명**: 로그인한 사용자가 작성한 댓글 목록을 페이지네이션으로 조회합니다

---

## 5. Follow API

### Base URL: `/api/follow`

#### 5.1 팔로우
- **Method**: `POST`
- **Path**: `/`
- **Request Body**: `FollowingRequest`
  - `id` (Long) - 팔로우할 사용자 ID
- **Response**: `ResponseEntity<Long>`
- **설명**: 특정 사용자를 팔로우합니다

#### 5.2 언팔로우
- **Method**: `DELETE`
- **Path**: `/{id}`
- **Path Variables**:
  - `id` (Long, required) - 언팔로우할 사용자 ID
- **Response**: `ResponseEntity<Long>`
- **설명**: 특정 사용자를 언팔로우합니다

#### 5.3 팔로워 목록 조회
- **Method**: `GET`
- **Path**: `/{uid}/followers`
- **Path Variables**:
  - `uid` (Long, required) - 대상 사용자 ID
- **Request Parameters**:
  - `p` (int, optional, default: 1) - 페이지 번호
- **Response**: `ResponseEntity<FollowingListResponse>`
- **설명**: 특정 사용자를 팔로우하는 사용자 목록을 페이지네이션으로 조회합니다

#### 5.4 팔로잉 목록 조회
- **Method**: `GET`
- **Path**: `/{uid}/followings`
- **Path Variables**:
  - `uid` (Long, required) - 대상 사용자 ID
- **Request Parameters**:
  - `p` (int, optional, default: 1) - 페이지 번호
- **Response**: `ResponseEntity<FollowingListResponse>`
- **설명**: 특정 사용자가 팔로우하는 사용자 목록을 페이지네이션으로 조회합니다

---

## 6. Chat API

### Base URL: `/api/chatting`

#### 6.1 채팅방 입장
- **Method**: `POST`
- **Path**: `/participant/{rid}`
- **Path Variables**:
  - `rid` (Long, required) - 채팅방 ID
- **Response**: `String` (redirect URL)
- **설명**: 단체 채팅방에 참여합니다

#### 6.2 채팅방 나가기
- **Method**: `PATCH`
- **Path**: `/rooms/{roomId}/{chatterId}`
- **Path Variables**:
  - `roomId` (Long, required)
  - `chatterId` (Long, required)
- **Response**: `void`
- **설명**: 현재 참여 중인 채팅방에서 나갑니다

#### 6.3 채팅방 종료
- **Method**: `PATCH`
- **Path**: `/rooms/{roomId}`
- **Path Variables**:
  - `roomId` (Long, required)
- **Response**: `void`
- **설명**: 멘토 전용 - 자신이 개설한 채팅방을 종료합니다

#### 6.4 채팅방 생성
- **Method**: `POST`
- **Path**: `/room`
- **Request Parameters**:
  - `roomName` (String, required)
  - `userLimit` (int, required)
- **Response**: `void`
- **설명**: 멘토 전용 - 새로운 단체 채팅방을 생성합니다

#### 6.5 채팅방 목록 조회
- **Method**: `GET`
- **Path**: `/rooms`
- **Request**: 없음
- **Response**: `List<ChatRoom>`
- **설명**: 전체 단체 채팅방 목록을 조회합니다

---

## 7. Notification API

### Base URL: `/api/notifications`

#### 7.1 읽지 않은 알림 개수 조회
- **Method**: `GET`
- **Path**: `/unread-count`
- **Request**: 없음 (인증 필요)
- **Response**: `ResponseEntity<Integer>`
- **설명**: 로그인한 사용자의 읽지 않은 알림 개수를 조회합니다

#### 7.2 전체 알림 읽음 처리
- **Method**: `PATCH`
- **Path**: `/read-all`
- **Request**: 없음 (인증 필요)
- **Response**: `ResponseEntity<String>`
- **설명**: 로그인한 사용자의 모든 알림을 읽음 상태로 변경합니다

#### 7.3 특정 알림 읽음 처리
- **Method**: `PATCH`
- **Path**: `/{id}/read`
- **Path Variables**:
  - `id` (String, required) - 알림 ID
- **Response**: `ResponseEntity<String>`
- **설명**: 특정 알림을 읽음 상태로 변경합니다

#### 7.4 읽은 알림 삭제
- **Method**: `DELETE`
- **Path**: `/delete`
- **Request**: 없음 (인증 필요)
- **Response**: `ResponseEntity<String>`
- **설명**: 테스트용 - 로그인한 사용자의 읽은 알림을 모두 삭제합니다

#### 7.5 읽지 않은 알림 목록 조회
- **Method**: `GET`
- **Path**: `/unread`
- **Request**: 없음 (인증 필요)
- **Response**: `ResponseEntity<SuccessResponse>` (List<Notification>)
- **설명**: 로그인한 사용자의 읽지 않은 알림 목록을 날짜순으로 조회합니다

---

## 8. User Scrape API

### Base URL: `/api/posts`

#### 8.1 내 스크랩 목록 조회
- **Method**: `GET`
- **Path**: `/myScrape`
- **Request Parameters**:
  - `page` (int, optional, default: 1)
  - `postCategory` (String, optional)
- **Response**: `ResponseEntity<PaginationResult>`
- **설명**: 로그인한 사용자가 스크랩한 게시물 목록을 페이지네이션으로 조회합니다

---

## 9. FCM Token API

### Base URL: `/api/fcm/token`

#### 9.1 FCM 토큰 등록/업데이트
- **Method**: `POST`
- **Path**: `/`
- **Request Body**: `FcmTokenRequest` (Valid)
  - `token` (String)
- **Response**: `ResponseEntity<SuccessResponse>`
- **설명**: 사용자의 FCM 토큰을 등록하거나 업데이트합니다. 앱 시작 시 호출

#### 9.2 FCM 토큰 비활성화
- **Method**: `DELETE`
- **Path**: `/`
- **Request Body**: `FcmTokenRequest` (Valid)
  - `token` (String)
- **Response**: `ResponseEntity<SuccessResponse>`
- **설명**: 특정 토큰을 비활성화합니다. 로그아웃 시 호출

#### 9.3 활성 토큰 조회
- **Method**: `GET`
- **Path**: `/active`
- **Request**: 없음 (인증 필요)
- **Response**: `ResponseEntity<SuccessResponse>` (List<String>)
- **설명**: 로그인한 사용자의 활성화된 FCM 토큰 목록을 조회합니다

#### 9.4 비활성 토큰 삭제
- **Method**: `DELETE`
- **Path**: `/deactivate`
- **Request**: 없음
- **Response**: `ResponseEntity<SuccessResponse>` (int - 삭제된 개수)
- **설명**: 관리자용 - 비활성화된 모든 FCM 토큰을 물리적으로 삭제합니다

---

## 10. SMS API

### Base URL: `/api/sms`

#### 10.1 멘토 SMS 알림 전송
- **Method**: `POST`
- **Path**: `/send-mento`
- **Request Body**: `SmsNotificationSendRequest` (Valid, ModelAttribute)
  - `receiverId` (Long)
- **Response**: `void`
- **설명**: 멘토에게 SMS 알림을 전송합니다

#### 10.2 읽지 않은 알림 SMS 전송
- **Method**: `POST`
- **Path**: `/send-unread`
- **Request Body**: `SmsNotificationSendRequest` (Valid, ModelAttribute)
  - `receiverId` (Long)
- **Response**: `void`
- **설명**: 읽지 않은 알림이 있을 때 SMS를 전송합니다

---

## 11. Image API

### Base URL: `/api/images`

#### 11.1 Summernote 이미지 업로드
- **Method**: `POST`
- **Path**: `/summernote`
- **Request Parameters**:
  - `file` (MultipartFile, required)
- **Response**: `ResponseEntity<ImageResponse>`
  - `url` (String) - 업로드된 이미지 URL
- **설명**: Summernote 에디터에서 이미지 업로드 시 호출되는 API

---

## 12. SSE (Server-Sent Events) API

### Base URL: `/api/sse`

#### 12.1 댓글 SSE 구독
- **Method**: `GET`
- **Path**: `/posts/{postId}/comments`
- **Path Variables**:
  - `postId` (Long, required)
- **Response**: `SseEmitter` (MediaType: TEXT_EVENT_STREAM)
- **설명**: 특정 게시글의 댓글 실시간 업데이트를 구독합니다

#### 12.2 좋아요 SSE 구독
- **Method**: `GET`
- **Path**: `/posts/{postId}/likes`
- **Path Variables**:
  - `postId` (Long, required)
- **Response**: `SseEmitter` (MediaType: TEXT_EVENT_STREAM)
- **설명**: 특정 게시글의 좋아요 실시간 업데이트를 구독합니다

#### 12.3 알림 SSE 구독
- **Method**: `GET`
- **Path**: `/notifications`
- **Request**: 없음 (인증 필요)
- **Response**: `ResponseEntity<SseEmitter>` (MediaType: TEXT_EVENT_STREAM)
- **설명**: 로그인한 사용자의 알림 실시간 업데이트를 구독합니다

---

## 공통 사항

### 인증
- 대부분의 API는 `@CurrentUser User user` 어노테이션을 통해 현재 로그인한 사용자 정보를 요구합니다
- JWT 토큰 기반 인증을 사용합니다
- Access Token은 쿠키로 전달됩니다 (httpOnly=false)
- Refresh Token은 쿠키로 전달됩니다 (httpOnly=true, path=/api/auth)

### 응답 형식

#### SuccessResponse
```json
{
  "status": 200,
  "message": "Success message",
  "data": {}
}
```

#### TotyResponse
```json
{
  "status": 200,
  "message": "Success",
  "data": {}
}
```

#### PaginationResult
```json
{
  "content": [],
  "page": 1,
  "size": 10,
  "totalPages": 5,
  "totalElements": 50
}
```

### 페이지네이션
- 대부분의 목록 조회 API는 `page` 파라미터를 지원합니다
- 기본값은 일반적으로 1입니다 (0-based가 아닌 1-based)
- 페이지 크기는 API마다 다릅니다 (일반적으로 10 또는 20)

### 파일 업로드
- `MultipartFile` 형식으로 파일을 업로드합니다
- 이미지 업로드 시 S3에 저장되며 URL이 반환됩니다

### 에러 응답
```json
{
  "status": 400,
  "message": "Error message",
  "error": "BAD_REQUEST"
}
```

### HTTPS 필수
- 모든 API는 HTTPS를 통해서만 접근 가능합니다
- Base URL: `https://toty.cloud`

---

**총 엔드포인트 수**: 약 60개
**마지막 업데이트**: 2026-01-09