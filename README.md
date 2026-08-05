# AdoptLink
> 유기동물 분양 및 보호소 안내 안드로이드 앱 서비스

## 프로젝트 소개
유기동물 분양 게시글 작성, 실시간 채팅, 보호소 관리자 신청 등의 기능을 제공하는 안드로이드 앱 서비스입니다.

- **개발 기간**: 2025.05 ~ 2026.07
- **개발 인원**: 1인

---

## 기술 스택

**Backend**
- Java 17, Spring Boot 3, Spring Security, Spring Data JPA
- WebSocket + STOMP

**Database & Cache**
- PostgreSQL, Redis

**Infra / Cloud**
- AWS EC2, RDS, S3

**Tools**
- Git, GitHub, Notion (API 명세서)

---

## 주요 기능

### 👤 회원
- 휴대폰 SMS 인증 기반 회원가입 (Redis TTL 5분 자동 만료)
- JWT Access Token + Refresh Token 이중 구조 인증
- 프로필 수정, 회원탈퇴 (Soft Delete)
- 유저 차단

### 📋 게시글
- 유기동물 분양 게시글 CRUD
- 키워드 기반 게시글 검색 (offset 페이지네이션)
- 게시글 스크랩 (토글)
- 분양 상태 변경 (AVAILABLE / COMPLETED)
- 이미지 업로드 (S3 직접 업로드 방식)

### 💬 채팅
- WebSocket + STOMP 기반 실시간 채팅
- 채팅방 생성, 목록 조회, 메시지 조회
- 채팅방 숨기기 (개인별 처리)
- 차단된 유저와 채팅 불가

### 🏠 보호소
- 보호소 관리자 신청 (증빙 이미지 첨부)
- 관리자 승인/반려 처리
- 승인 시 유저 Role 자동 변경 (USER → SHELTER_ADMIN)

### 📢 공지사항
- 관리자 전용 공지사항 CRUD
- 일반 유저 조회 가능

### 🔔 알림 설정
- 알림 설정 조회/변경 (채팅, 공지, 마케팅)
- 회원가입 시 기본 알림 설정 자동 생성

---

## 인증/보안 설계

- 로그인 시 Access Token (30분) + Refresh Token (14일) 발급
- Access Token 만료 시 Refresh Token으로 재발급
- 로그아웃 시 Refresh Token DB에서 삭제
- **RBAC 권한 관리**: USER / SHELTER_ADMIN / ADMIN 계층 구조
- **BCrypt** 비밀번호 암호화
- **관리자 계정**: 별도 Admin 테이블 + 전용 로그인 API

---

## 휴대폰 인증 플로우

1. 휴대폰 번호 입력 → 솔라피 SMS 발송
2. 인증번호 Redis에 저장 (TTL 5분)
3. 인증번호 확인 → verificationId 발급 (TTL 10분)
4. 회원가입 시 verificationId 검증

---

## 트러블슈팅

### 1. SMS API SignatureDoesNotMatch 에러
- **문제**: 솔라피 SMS API 호출 시 서명 불일치 에러 발생
- **원인 1**: CoolSMS SDK가 솔라피로 변경되면서 패키지명 변경 (`net.nurigo` → `com.solapi`)
- **원인 2**: `@Value` 어노테이션에 닫는 `}` 누락으로 API Secret이 주입되지 않음
- **해결**: SDK 교체 및 어노테이션 오타 수정

### 2. 채팅방 숨기기 설계
- **문제**: Soft Delete로 구현 시 상대방 채팅방도 함께 사라지는 문제
- **해결**: `ChatRoomMember` 엔티티에 `hidden` 필드 추가해 사용자별 개별 처리
- **추가**: 상대방 메시지 수신 시 숨긴 채팅방 자동 재노출

### 3. Redis 활용 판단
- **문제**: 인증번호를 DB에 저장 시 만료 처리 로직이 복잡해짐
- **해결**: Redis TTL 기능으로 5분 후 자동 삭제 처리

### 4. WebSocket STOMP 동적 경로
- **문제**: `@SendTo` 어노테이션이 동적 경로(`{roomId}`)를 지원하지 않음
- **해결**: `SimpMessagingTemplate`으로 동적 경로에 직접 브로드캐스트

---

## 📁 패키지 구조
```
src/main/java/animals/demo/
├── auth/          # 인증 (로그인, 회원가입, JWT)
├── user/          # 유저 관련
├── post/          # 게시글 관련
├── chat/          # 채팅 관련
├── notification/  # 알림 설정
├── notice/        # 공지사항
├── facility/      # 보호소 관련
├── admin/         # 관리자
├── security/      # Spring Security, JWT 필터
└── common/        # 공통 (ApiResponse, Exception 등)
```
---

