package animals.demo.auth;

import animals.demo.user.entity.AuthProvider;
import animals.demo.user.entity.Role;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/*
탈퇴 회원의 로그인·토큰 사용 여부 확인용 재현 테스트
- 탈퇴 전 로그인과 탈퇴 요청은 전제 조건이므로 검증한다.
- 탈퇴 이후 동작은 판정하지 않고, 실제 HTTP 응답과 DB 상태를 콘솔에 출력한다.
- 실제 DB 대신 테스트 전용 H2 메모리 DB를 사용한다.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:withdrawn-login-repro;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=adoptlink-test-only-secret-key-at-least-32-bytes",
        "jwt.access-token-expiration=3600000",
        "jwt.refresh-token-expiration=1209600000",
        "coolsms.api-key=test-key",
        "coolsms.api-secret=test-secret",
        "coolsms.sender-phone=01000000000"
})
class WithdrawnUserLoginReproductionTest {

    private static final String LOGIN_ID = "withdrawnUser";
    private static final String PASSWORD = "password1234!";

    @Autowired WebApplicationContext context;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;

    MockMvc mockMvc;

    @Test
    void 탈퇴_회원_로그인_재현() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        User user = userRepository.save(User.builder()
                .loginId(LOGIN_ID).nickname("withdrawn").phone("01033330000")
                .role(Role.USER).provider(AuthProvider.LOCAL)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .build());

        System.out.println("\n==================== 탈퇴 회원 로그인 재현 ====================");
        System.out.println("userId=" + user.getUserId() + ", loginId=" + LOGIN_ID);

        //1. 탈퇴 전 로그인 (전제 조건)
        MvcResult firstLogin = call("[1] 탈퇴 전 로그인  POST /api/auth/login", null, loginRequest());
        assertEquals(200, firstLogin.getResponse().getStatus(), "탈퇴 전 로그인은 성공해야 한다");
        String body = firstLogin.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String accessToken = JsonPath.read(body, "$.data.accessToken");
        String refreshToken = JsonPath.read(body, "$.data.refreshToken");

        //2. 회원 탈퇴 (전제 조건)
        MvcResult withdraw = call("[2] 회원 탈퇴  DELETE /api/users/me", accessToken, delete("/api/users/me"));
        assertEquals(200, withdraw.getResponse().getStatus(), "탈퇴 요청은 성공해야 한다");

        printDbState("[3] 탈퇴 직후 DB 상태", user.getUserId());
        assertNotNull(jdbcTemplate.queryForObject(
                "select deleted_at from users where user_id = ?", Object.class, user.getUserId()),
                "탈퇴하면 deleted_at이 기록되어야 한다");

        //3. 탈퇴 이후 동작 (판정하지 않고 기록만 함)
        call("[4] 탈퇴 전에 받은 access token으로 내 정보 조회  GET /api/users/me",
                accessToken, get("/api/users/me"));
        call("[5] 탈퇴 전에 받은 refresh token으로 재발급  POST /api/auth/reissue",
                refreshToken, post("/api/auth/reissue"));
        MvcResult reLogin = call("[6] 탈퇴 후 같은 계정으로 다시 로그인  POST /api/auth/login",
                null, loginRequest());

        if (reLogin.getResponse().getStatus() == 200) {
            String newAccess = JsonPath.read(
                    reLogin.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.data.accessToken");
            call("[7] 재로그인으로 받은 access token으로 내 정보 조회  GET /api/users/me",
                    newAccess, get("/api/users/me"));
        }

        printDbState("[8] 최종 DB 상태", user.getUserId());
        System.out.println("=================================================================\n");
    }

    private MockHttpServletRequestBuilder loginRequest() {
        return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"" + LOGIN_ID + "\",\"password\":\"" + PASSWORD + "\"}");
    }

    private MvcResult call(String title, String token, MockHttpServletRequestBuilder request) throws Exception {
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        MvcResult result = mockMvc.perform(request).andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8)
                .replaceAll("\"(accessToken|refreshToken)\":\"[^\"]+\"", "\"$1\":\"(발급됨)\"");
        System.out.println("\n" + title);
        System.out.println("  HTTP " + result.getResponse().getStatus());
        System.out.println("  " + body);
        return result;
    }

    private void printDbState(String title, Long userId) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "select user_id, login_id, deleted_at from users where user_id = ?", userId);
        List<Map<String, Object>> tokens = jdbcTemplate.queryForList(
                "select user_id from refresh_token where user_id = ?", userId);
        System.out.println("\n" + title);
        users.forEach(row -> System.out.println("  users: " + row));
        System.out.println("  refresh_token 저장 여부: " + (tokens.isEmpty() ? "없음" : "있음 (" + tokens.size() + "건)"));
    }
}
