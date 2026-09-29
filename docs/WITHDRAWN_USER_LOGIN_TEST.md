# 탈퇴 회원 로그인 재현 테스트

작성일: 2026-09-29

## 목적

회원 탈퇴 후에도 같은 계정으로 로그인하거나, 탈퇴 전에 받은 토큰으로 서비스를 계속 이용할 수 있는지 확인한다. 코드는 수정하지 않고 현재 동작만 기록했다.

## 확인 전 코드 분석

- 회원 탈퇴(`UserService.signOut`)는 `users.deleted_at`만 기록하는 soft delete다.
- 로그인(`AuthService.login`)은 `findByLoginId`로 사용자를 찾고 비밀번호만 비교한다. 탈퇴 여부는 확인하지 않는다.
- 토큰 재발급(`AuthService.reissue`)도 저장된 refresh token과 일치하는지만 확인하고 탈퇴 여부는 확인하지 않는다.
- 탈퇴할 때 저장된 refresh token을 삭제하지 않는다.

## 테스트 방법

- 테스트: `src/test/java/animals/demo/auth/WithdrawnUserLoginReproductionTest.java`
- 환경: `@SpringBootTest`, 테스트 전용 H2 메모리 DB, MockMvc로 실제 REST API와 Spring Security 필터를 거쳐 호출
- 사용자는 DB에 직접 저장하고(비밀번호는 실제 `PasswordEncoder`로 암호화), 이후 과정은 모두 API로 호출했다.
- 탈퇴 전 로그인과 탈퇴 요청은 전제 조건이므로 200인지 검증한다. 탈퇴 이후 동작은 판정하지 않고 실제 응답과 DB 상태를 출력한다.
- 출력에서 토큰 값은 `(발급됨)`으로 가린다.

실행:

```sh
./gradlew test --tests 'animals.demo.auth.WithdrawnUserLoginReproductionTest' -i
```

## 결과 (2026-09-29, 수정 전 코드 기준)

기준 커밋: `db868c0` (feature), `BUILD SUCCESSFUL`

| 단계 | 요청 | 결과 | 기대 동작 |
|---|---|---|---|
| 1 | 탈퇴 전 로그인 `POST /api/auth/login` | 200, 토큰 발급 | 200 |
| 2 | 회원 탈퇴 `DELETE /api/users/me` | 200 | 200 |
| 3 | 탈퇴 직후 DB | `deleted_at` 기록됨, refresh token **1건 남아 있음** | refresh token 삭제 |
| 4 | 탈퇴 전 access token으로 내 정보 조회 `GET /api/users/me` | **200**, 로그인 ID·닉네임·전화번호 반환 | 401 또는 404 |
| 5 | 탈퇴 전 refresh token으로 재발급 `POST /api/auth/reissue` | **200**, 새 토큰 발급 | 401 |
| 6 | 같은 계정으로 다시 로그인 | **200**, 새 토큰 발급 | 로그인 거절 |
| 7 | 재로그인 토큰으로 내 정보 조회 | **200**, 개인정보 반환 | 요청 불가 |
| 8 | 최종 DB | `deleted_at` 유지, refresh token 1건 | - |

## 확인된 문제

1. **탈퇴한 회원이 다시 로그인할 수 있다.** 탈퇴는 `deleted_at`만 기록하고, 로그인은 이를 확인하지 않는다.
2. **탈퇴 전에 받은 refresh token으로 계속 재발급받을 수 있다.** 탈퇴 시 refresh token을 삭제하지 않고, 재발급도 탈퇴 여부를 확인하지 않는다.
3. **탈퇴 전에 받은 access token으로 개인정보가 조회된다.** 필터는 토큰 서명과 만료만 확인하고, `getMyInfo`는 `findById`로 탈퇴한 사용자도 조회한다.

결과적으로 사용자에게는 "정상적으로 탈퇴되었습니다."라고 응답하지만, 계정은 로그인·토큰 재발급·개인정보 조회가 모두 가능한 상태로 남는다.

## 수정 방향 (미적용)

- 로그인: 탈퇴한 사용자는 거절한다. 탈퇴 여부를 드러내지 않도록 기존 `ID_PASSWORD_MISMATCH`를 그대로 쓰는 방안을 검토한다.
- 탈퇴: 같은 트랜잭션에서 저장된 refresh token을 삭제한다.
- 재발급: 탈퇴한 사용자면 거절한다.
- 탈퇴 전에 발급된 access token: 만료 전까지 유효한 JWT 특성상, 사용자 조회를 탈퇴하지 않은 사용자로 제한하거나 필터·서비스에서 탈퇴 여부를 확인한다. access token 만료 시간을 짧게 유지하는 것도 함께 고려한다.

수정 후에는 이 테스트의 4~7단계가 거절되는지 판정하는 검증을 추가해 회귀 테스트로 전환한다.

## 참고

- 이 테스트는 탈퇴 이후 동작을 판정하지 않으므로 수정 전후 모두 통과로 표시된다. 결과는 콘솔 출력으로 확인한다.
