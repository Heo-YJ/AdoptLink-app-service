# REST 인증·게시글 수정 변경 사항

작성일: 2026-09-26

## 문제와 수정

- REST 필터는 서명·만료만 확인해 refresh token도 인증 객체로 만들었다. 이제 ACCESS 용도, 계정 종류, 허용된 역할, 유효한 숫자 ID와 만료를 함께 검사한다.
- 관리자 계정과 일반 회원은 ID가 같은 숫자여도 서로 다른 계정이다. ADMIN의 일반 회원 권한 상속을 제거하고 일반 회원 API는 USER/SHELTER_ADMIN만 허용한다.
- 일반 회원과 관리자의 refresh token에도 계정 종류를 넣는다. 일반 회원 재발급 API는 USER의 REFRESH 토큰만 허용하며 DB에 저장된 토큰과 일치하는지 확인한다.
- 재발급 요청은 REST access token 필터에서 제외하고 AuthService에서 refresh token을 검증한다. 누락되거나 잘못된 Bearer 헤더도 401로 처리한다.
- 필터 단계의 인증 오류와 접근 거절도 ApiResponse 형식으로 반환한다.
- 게시글 수정 Controller가 Service에 전달하던 userId/postId 순서를 바로잡았다. 작성자 본인 확인은 유지한다.
- STOMP의 기존 validateToken 호출도 강화된 access token 검증을 사용한다. 인터셉터의 참여자·역할·목적지 제한은 변경하지 않는다.

## 클라이언트 반영

| 상황 | 사용 토큰 / 응답 |
|---|---|
| 일반 REST API | 일반 회원·보호소 관리자의 access token |
| 관리자 API | 관리자의 access token |
| POST /api/auth/reissue | 일반 회원의 refresh token을 Authorization Bearer 헤더로 전달 |
| STOMP CONNECT | 일반 회원·보호소 관리자의 access token, 기존 경로 유지 |
| 누락·잘못된 access token·만료된 access token | 401 |
| 올바른 access token이지만 해당 API 권한 없음 | 403 |
| 만료된 refresh token | 기존 계약인 403 유지 |

새 JWT는 tokenType(ACCESS/REFRESH), subjectType(USER/ADMIN)을 포함한다.
보호소 관리자의 subjectType은 USER이고 role은 SHELTER_ADMIN이다.
이전 클레임 형식의 토큰은 허용하지 않으므로 배포 후 기존 토큰을 삭제하고 재로그인해야 한다.
access token 만료 401 이후 유효한 refresh token으로 재발급하고, 재발급 거절 시 로그인 화면으로 안내한다.
관리자 전용 재발급 API를 새로 추가한 변경은 아니다.

## 회귀 테스트

`RestSecurityRegressionTests`는 실제 SecurityFilterChain·JWT 서명 검증·Controller·Service를 MockMvc로 연결한다.
Repository, Redis, SMS는 mock으로 대체해 외부 DB 연결이나 실제 문자 발송을 하지 않는다.

- 일반 회원·보호소 관리자 토큰 허용, 사용자 ID 유지.
- refresh token의 일반 API 인증 차단.
- 관리자 토큰의 일반 회원 신원 사용 차단, 일반 회원의 관리자 API 접근 차단.
- 무토큰·위조 서명·만료·기존 형식·잘못된 클레임 거절.
- 저장된 일반 회원 refresh token의 정상 재발급.
- access token·관리자 refresh token·미등록·저장값 불일치·만료·잘못된 헤더 재발급 거절.
- 사용자 ID와 게시글 ID가 달라도 본인 글 수정 성공, 다른 작성자의 수정은 거절.

실행 명령:

```sh
./gradlew test --tests animals.demo.security.RestSecurityRegressionTests --tests animals.demo.chat.ChatRegressionTests --no-daemon
./gradlew build --no-daemon
git diff --check
```

## 현재 검증 상태

- 2026-09-25에 시작한 회귀 테스트 명령은 compileJava에서 실패했다. 원인은 ChatChannelInterceptor.java의 iCloud 다운로드 실패(Operation canceled)이다.
- 테스트 실행 전의 환경 실패이므로 추가한 테스트의 통과·실패를 판정할 수 없다.
- 2026-09-26 재개 시에도 해당 파일은 dataless 상태였다. Finder에서 해당 파일과 demo 폴더의 지금 다운로드를 요청했다.
- 전체 테스트·빌드·저장소 전체 diff 검사는 아직 완료되지 않았다.
- 실제 PostgreSQL·WebSocket·웹/안드로이드 클라이언트 연동·SMS 발송은 수행하지 않았다.

게시글 비로그인 공개, 탈퇴 계정·삭제 게시글 정책, 보호소 등록 기능은 이번 수정 범위에 포함하지 않는다.
