# 채팅 추가 통합 테스트 결과

## 1. 실행 정보

- 실행일: 2026-09-11 (KST)
- 사용자 요청: 기존 결과를 삭제하고 아래 네 범위의 새로운 실행 결과로 대체
- 지침: `AGENTS.md`를 읽고 준수
- 코드 기준: `45c2a6fe8781434cd78cf196fc683c6b0a205c10` 및 현재 작업 트리
- 기존 변경 보존: AGENTS.md, docker-compose.yml, docs/, TestPasswordHashGenerator.java 등
- 애플리케이션 소스 수정: 없음
- 실행 준비: `./gradlew bootJar -x test --no-daemon` → BUILD SUCCESSFUL, 6m 57s
- 이 명령은 테스트용 실행 JAR 생성이다. 기존 단위 테스트를 재실행하거나 전체 build를 수행한 결과가 아니다.
- 주요 통신 검증: 20:26~20:27
- 별도 백엔드 재시작: 20:31:41 시작 완료, 이후 브라우저와 SQL로 재검증

## 2. 실제 검증 환경

| 구성 | 환경 |
|---|---|
| 백엔드 | 현재 소스 기준 JAR, Java 17, 별도 포트 127.0.0.1:18089 |
| DB | 사용자 Docker PostgreSQL 18.6, 호스트 포트 5433 |
| 격리 방식 | 같은 PostgreSQL 컨테이너 안에 이번 실행 전용 DB `codex_followup_20260911` 생성 |
| 기존 환경 | 사용자 8080 서버 및 `adoptlink` DB는 중지·수정하지 않음 |
| Redis 설정 | localhost:6380. 이번 채팅 테스트는 Redis 저장을 요구하지 않으며 Redis 별도 검증은 수행하지 않음 |
| 설정 | 임시 properties로 외부 기본 설정을 대체, ddl-auto=update |
| 클라이언트 | Codex In-app Browser의 실제 WebSocket/STOMP 및 fetch REST 요청 |
| 계정 | 전용 DB에 새 A/B/C fixture 생성, 게시글 ID 1, REST로 채팅방 ID 1 생성 |
| 토큰 | 별도 서버 전용 테스트 키로 유효 토큰 및 서명이 유효한 만료 토큰 생성 |

로그인·회원가입·SMS 검증은 이번 범위에 포함하지 않았다.
프로젝트 test.html을 변경하지 않고 임시 검증 페이지를 사용했다.
raw WebSocket 경로 `/ws/chat/websocket`, STOMP CONNECT/SUBSCRIBE/SEND를 사용했다.
기존 test.html의 SockJS UI 및 HTTP fallback 검증 결과를 의미하지 않는다.

## 3. 결과 요약

| 요청 범위 | 상태 | 실제 결과 |
|---|---|---|
| C가 구독 없이 전송만 시도할 때 저장·전달 차단 | 통과 | C CONNECT 성공 후 SUBSCRIBE 없이 SEND. A/B 미수신 및 REST·SQL 미저장 확인. 재시작 후 다시 확인 |
| 채팅방 숨김·복구, 기존 방 재사용 | 통과 | B만 목록에서 숨겨짐, A 전송으로 복구, 다시 숨긴 뒤 재문의 시 같은 roomId 반환 및 목록 복구 |
| 재연결 및 서버 재시작 후 데이터 유지 | 통과 | B 연결 종료·재연결·재구독 후 정상 수신. 별도 백엔드 재시작 후 메시지 5개와 숨김 상태 및 마지막 메시지 유지 |
| 만료 토큰·발신자 위조·사용자 차단 관계 | 통과 | 만료 토큰 연결 거절, B가 A의 ID를 보내도 B로 기록, A가 B를 차단한 후 양쪽 전송이 미저장·미전달 |

요청한 네 범위에서 미검증 항목은 없다.
단, 안드로이드·SMS·운영 DB 및 장시간 부하·지연 테스트는 이번 실행 범위 밖이다.

## 4. 세부 절차 및 근거

### 4.1 C의 구독 없는 전송

1. A/B가 유효한 토큰으로 CONNECT 후 방 1을 SUBSCRIBE했다.
2. 정상 송수신을 먼저 확인하여 수신 클라이언트의 동작을 확인했다.
3. C는 자신의 유효한 토큰으로 CONNECT 성공을 확인했다.
4. C에게 SUBSCRIBE를 보내지 않고 `/app/chat/message`로 `e2e-C-denied`를 전송했다.
5. 3초 관찰 후 A/B 수신 목록에 해당 메시지가 없고 REST 메시지 조회에도 없는 것을 확인했다.
6. 서버 로그의 `CustomException: 해당 작업에 대한 권한이 없습니다.`를 확인했다.
7. 재시작 후에도 새 연결로 `e2e-C-recheck`를 전송하여 동일하게 미수신·미저장을 확인했다.

결과: 요청된 보안 기준인 미저장·미전달 충족.
서비스 단계 권한 예외에 대한 클라이언트의 명시적인 오류 안내는 기존 제약으로 남아 있다.
따라서 STOMP ERROR 프레임 수신이나 연결 종료를 이 테스트의 필수 통과 조건으로 삼지 않았다.

### 4.2 숨김·복구·기존 방 재사용

1. B가 `PATCH /api/chatrooms/1/hide`를 호출했다.
2. B의 목록에서 방이 빠지고 A의 목록에는 유지되는지 확인했다.
3. A가 `e2e-unhide`를 전송하여 B의 수신과 목록 복구를 확인했다.
4. B가 다시 숨긴 뒤 `POST /api/chatrooms`에 동일 postId를 보내 재문의했다.
5. 기존 roomId=1 반환과 B 목록 복구를 확인했다.
6. 최종 SQL에서 chat_rooms는 1행임을 확인했다.

결과: 사용자별 숨김, 새 메시지에 따른 복구, 기존 방 재사용 통과.

### 4.3 재연결

1. A/B 정상 송수신 확인 후 B의 WebSocket 연결을 종료했다.
2. B를 다시 CONNECT하고 방 1을 새로 SUBSCRIBE했다.
3. B가 `e2e-reconnect`를 전송했다.
4. A/B에서 해당 메시지가 각 1회 수신되고 DB에는 1행만 저장되는지 확인했다.

결과: 연결을 새로 만든 뒤 재구독하는 흐름 통과.
이는 실제 안드로이드 앱의 자동 재연결 또는 네트워크 전환을 검증한 것은 아니다.

### 4.4 만료 토큰

1. 이번 테스트 키로 서명된 토큰을 생성하되 exp를 현재 시각보다 60초 이전으로 설정했다.
2. 새 WebSocket 연결에서 해당 토큰으로 STOMP CONNECT를 시도했다.
3. CONNECTED가 수신되지 않고 ERROR 또는 연결 종료가 발생하는 것을 확인했다.

결과: 연결 시점의 만료 토큰 거절 통과.
이미 연결된 세션에서 토큰 만료 후 자동 종료되는지는 이번 범위가 아니다.

### 4.5 발신자 위조

1. B로 인증된 연결에서 본문의 senderUserId를 A의 ID(1)로 설정했다.
2. `e2e-spoof`를 전송했다.
3. 수신 메시지와 REST·SQL 조회에서 senderUserId가 B의 ID(2)인지 확인했다.

결과: 실제 인증된 사용자로 덮어쓰기 통과.

### 4.6 사용자 차단 관계

1. A가 `POST /api/users/2/block`을 호출했다.
2. A가 `e2e-block-A`, B가 `e2e-block-B`를 각각 전송했다.
3. 각 전송 후 3초 관찰하여 A/B 미수신 및 REST 미저장을 확인했다.
4. 서버 로그에서 `CustomException: 더 이상 채팅을 보낼 수 없습니다.`가 두 차례 발생함을 확인했다.

결과: 한쪽이 차단한 관계에서 양쪽 모두 전송 차단.

### 4.7 백엔드 재시작 후 영속성

1. 마지막으로 B의 방을 숨기고 REST와 SQL 상태를 확인했다.
2. 이번에 띄운 별도 백엔드만 정상 종료했다. PostgreSQL은 계속 실행했다.
3. 동일 설정·동일 DB로 새 백엔드 프로세스를 실행했다.
4. 새 브라우저 요청으로 메시지 개수, 발신자, 마지막 메시지와 B 목록 제외를 확인했다.
5. 아래 SQL 조회를 다시 수행하여 재시작 전후 값을 비교했다.

```sql
SELECT message_id, sender_user_id, content, created_at
FROM messages ORDER BY message_id;
SELECT room_id, last_message, last_message_at FROM chat_rooms;
SELECT user_id, hidden FROM chat_room_members ORDER BY user_id;
```

재시작 전후 동일한 메시지:

| ID | 발신자 ID | 내용 | created_at |
|---|---|---|---|
| 1 | 2 | e2e-B | 2026-09-11 20:26:52.578376 |
| 2 | 1 | e2e-A | 2026-09-11 20:26:52.669393 |
| 3 | 2 | e2e-reconnect | 2026-09-11 20:26:53.149534 |
| 4 | 1 | e2e-unhide | 2026-09-11 20:26:53.350611 |
| 5 | 2 | e2e-spoof | 2026-09-11 20:26:56.710585 |

- 방 ID: 1
- 마지막 메시지: e2e-spoof
- 마지막 메시지 시각: 2026-09-11 20:26:56.712248
- A hidden=false, B hidden=true
- C 전송·차단 관계 전송 메시지는 저장되지 않음

결과: 백엔드 재시작 후 저장 데이터와 사용자별 숨김 상태 유지 통과.
PostgreSQL 컨테이너 자체 재시작은 이 테스트에 포함하지 않았다.

## 5. 검증 방식의 한계

- 정상 메시지 수신 대기는 최대 3초, 미수신 관찰은 각 3초였다.
- 장시간 지연이나 부하 상황의 미수신을 보장하는 결과가 아니다.
- 사용자가 직접 만든 계정과 대화는 사용하지 않고 동일 Docker PostgreSQL의 별도 DB에서 검증했다.
- 소스 수정 없이 검증했으며, 오류 안내 방식도 변경하지 않았다.
- 서비스 예외는 서버에 기록되지만 클라이언트가 명시적 실패 알림을 받지 못하는 제약은 별도 개선 과제다.

## 6. 정리 및 산출물

- 요청에 따라 TEST_RESULTS.md의 이전 내용을 이번 결과로 대체했다.
- `git diff --check` 통과. 새 결과 문서의 줄 끝 공백도 별도로 확인했다.
- 이번에 실행한 별도 백엔드 프로세스를 종료했다.
- 이번 실행 전용 DB `codex_followup_20260911`를 삭제해 테스트 데이터와 차단 관계를 함께 정리했다.
- 사용자님의 기존 `adoptlink` DB, Docker PostgreSQL·Redis 컨테이너와 8080 서버는 유지했다.
- 임시 브라우저 검증 탭을 닫았다.
- 임시 페이지·로그는 /private/tmp에 남아 있으며 버전 관리에 포함하지 않는다.
- 로그: `/private/tmp/adoptlink-followup.log`, `/private/tmp/adoptlink-followup-restart.log`
- 애플리케이션 코드, 기존 test.html, 기존 계정·대화 데이터는 변경하지 않았다.

---

## 2026-09-26 REST 인증·게시글 수정 회귀 검증 시도

위 2026-09-11 기록은 당시 채팅 검증 결과이며 아래 수정 코드의 통과 근거가 아니다.

- 코드 기준: HEAD `45c2a6f`와 현재 미커밋 변경. 기존 웹 기획 문서와 작업 지침 변경을 보존했다.
- 수정: JWT 용도·계정 종류 구분, 관리자/일반 회원 권한 분리, 인증 오류 응답, 재발급 토큰 검사, 게시글 수정 인자 순서.
- 환경: 로컬 macOS, Java 17.0.17, Gradle 9.4.1. 운영 서버·DB에 연결하거나 SMS를 발송하지 않았다.
- 신규 테스트: MockMvc의 실제 필터·Controller·Service와 mock Repository/Redis/SMS로 REST 동작을 검증하도록 작성했다.
- 실행 명령: `./gradlew test --tests animals.demo.security.RestSecurityRegressionTests --tests animals.demo.chat.ChatRegressionTests --no-daemon`.
- 실행 결과: 이전 턴에서 시작한 명령이 `compileJava`에서 종료 코드 1로 실패. 테스트 실행 전 실패이므로 테스트 결과는 미검증이다.
- 오류 근거: `error reading .../ChatChannelInterceptor.java; Operation canceled`. 해당 파일의 macOS 플래그는 `compressed,dataless`로 확인됐다.
- 재개 후 Finder에서 해당 파일과 프로젝트 `demo` 폴더의 `지금 다운로드`를 실행했으나 마지막 확인 시에도 파일 내용이 로컬에 내려오지 않았다.
- Git 상태·diff 조회도 iCloud 파일 읽기 대기 및 일부 `Operation canceled` 오류로 전체 결과를 확인하지 못했다.
- 변경한 파일의 줄 끝 공백은 별도 검사해 문제가 없음을 확인했다. 이를 저장소 전체 `git diff --check` 통과로 간주하지 않는다.
- 상태: 환경 문제로 회귀 테스트·전체 테스트·빌드 미검증. iCloud 다운로드 완료 후 위 회귀 테스트, `./gradlew build --no-daemon`, `git diff --check`를 수행해야 한다.
- 클라이언트 영향: 새 tokenType/subjectType 클레임이 없는 기존 토큰은 거절하므로 재로그인이 필요하다. 상세 계약은 `docs/AUTH_FIX_NOTES.md`에 기록했다.
- 실제 PostgreSQL·WebSocket·웹/안드로이드 클라이언트 통신 검증은 이번에 수행하지 않았다.
