package animals.demo.security;

import animals.demo.auth.controller.AuthController;
import animals.demo.auth.entity.RefreshToken;
import animals.demo.auth.repository.RefreshTokenRepository;
import animals.demo.auth.service.AuthService;
import animals.demo.common.GlobalExceptionHandler;
import animals.demo.common.sms.SmsService;
import animals.demo.notification.repository.NotificationRepository;
import animals.demo.post.controller.PostController;
import animals.demo.post.entity.Post;
import animals.demo.post.entity.PostAnimal;
import animals.demo.post.entity.Status;
import animals.demo.post.repository.PostAnimalRepository;
import animals.demo.post.repository.PostImageRepository;
import animals.demo.post.repository.PostRepository;
import animals.demo.post.repository.PostScrapRepository;
import animals.demo.post.service.PostService;
import animals.demo.security.config.SecurityConfig;
import animals.demo.user.entity.Role;
import animals.demo.user.entity.User;
import animals.demo.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig(RestSecurityRegressionTests.Config.class)
@TestPropertySource(properties = {
        "jwt.secret=adoptlink-rest-regression-test-only-secret-key",
        "jwt.access-token-expiration=60000",
        "jwt.refresh-token-expiration=600000"
})
class RestSecurityRegressionTests {
    private static final String TEST_SECRET = "adoptlink-rest-regression-test-only-secret-key";

    @Autowired private WebApplicationContext context;
    @Autowired private JwtTokenProvider tokens;
    @MockitoBean private UserRepository users;
    @MockitoBean private RefreshTokenRepository refreshTokens;
    @MockitoBean private RedisTemplate<String, String> redisTemplate;
    @MockitoBean private SmsService smsService;
    @MockitoBean private NotificationRepository notifications;
    @MockitoBean private PostRepository posts;
    @MockitoBean private PostAnimalRepository animals;
    @MockitoBean private PostImageRepository images;
    @MockitoBean private PostScrapRepository scraps;
    private MockMvc mvc;

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, JwtTokenProvider.class, SecurityErrorHandler.class,
            AuthController.class, AuthService.class, PostController.class, PostService.class,
            GlobalExceptionHandler.class, ProbeController.class})
    static class Config {
        @Bean ObjectMapper objectMapper() {
            return JsonMapper.builder().build();
        }
    }

    @RestController
    static class ProbeController {
        @GetMapping("/api/security-probe")
        Long user() { return SecurityUtils.getCurrentUserId(); }

        @GetMapping("/api/admin/security-probe")
        Long admin() { return SecurityUtils.getCurrentUserId(); }
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "SHELTER_ADMIN"})
    void userAccessTokensAuthenticateAsTheirSubject(String role) throws Exception {
        mvc.perform(get("/api/security-probe").header("Authorization", bearer(tokens.createAccessToken(7L, role))))
                .andExpect(status().isOk()).andExpect(content().string("7"));
    }

    @Test
    void refreshTokensCannotAuthenticateToUserApi() throws Exception {
        for (String token : new String[]{tokens.createRefreshToken(7L), tokens.createAdminRefreshToken(7L)}) {
            mvc.perform(get("/api/security-probe").header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("status").value(401))
                    .andExpect(jsonPath("message").value("유효하지 않은 Access Token입니다."));
        }
    }

    @Test
    void adminAccessTokenDoesNotInheritUserIdentity() throws Exception {
        String token = bearer(tokens.createAccessToken(7L, "ADMIN"));
        mvc.perform(get("/api/admin/security-probe").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(get("/api/security-probe").header("Authorization", token))
                .andExpect(status().isForbidden()).andExpect(jsonPath("status").value(403));
        mvc.perform(patch("/api/posts/42").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"changed\",\"content\":\"body\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(posts);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "SHELTER_ADMIN"})
    void userRolesCannotReachAdminApi(String role) throws Exception {
        mvc.perform(get("/api/admin/security-probe").header("Authorization", bearer(tokens.createAccessToken(7L, role))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousAndMalformedTokensReturnJsonUnauthorized() throws Exception {
        mvc.perform(get("/api/security-probe")).andExpect(status().isUnauthorized()).andExpect(jsonPath("status").value(401));
        mvc.perform(get("/api/security-probe").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("status").value(401));
        JwtTokenProvider otherSigner = new JwtTokenProvider("different-test-only-secret-key-at-least-32-bytes", 60000, 600000);
        mvc.perform(get("/api/security-probe").header("Authorization", bearer(otherSigner.createAccessToken(7L, "USER"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredAccessTokenReturns401InsteadOfEscapingTheFilter() throws Exception {
        JwtTokenProvider expired = new JwtTokenProvider(TEST_SECRET, -60000, 600000);
        mvc.perform(get("/api/security-probe").header("Authorization", bearer(expired.createAccessToken(7L, "USER"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value("만료된 Access Token입니다."));
    }

    @Test
    void legacyAndInvalidClaimsAreRejected() throws Exception {
        String legacy = Jwts.builder().subject("7").claim("role", "USER")
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        for (String token : new String[]{legacy, tokens.createAccessToken(7L, "UNKNOWN"), tokens.createAccessToken(-1L, "USER")}) {
            mvc.perform(get("/api/security-probe").header("Authorization", bearer(token))).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void inconsistentAccountTypeAndNonStringRoleAreRejected() throws Exception {
        for (Object role : new Object[]{"ADMIN", 7}) {
            String token = Jwts.builder().subject("7").claim("tokenType", "ACCESS")
                    .claim("subjectType", "USER").claim("role", role)
                    .expiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8))).compact();
            mvc.perform(get("/api/security-probe").header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("status").value(401));
        }
    }

    @Test
    void validStoredUserRefreshTokenStillReissuesTokens() throws Exception {
        String token = tokens.createRefreshToken(7L);
        User user = user(7L);
        when(refreshTokens.findByUserId(7L)).thenReturn(Optional.of(RefreshToken.builder().userId(7L).refreshToken(token).build()));
        when(users.findById(7L)).thenReturn(Optional.of(user));
        mvc.perform(post("/api/auth/reissue").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("data.accessToken").isNotEmpty())
                .andExpect(jsonPath("data.refreshToken").isNotEmpty());
        verify(refreshTokens).save(any(RefreshToken.class));
    }

    @Test
    void accessAndAdminRefreshTokensCannotBeReissuedAsUsers() throws Exception {
        for (String token : new String[]{tokens.createAccessToken(7L, "USER"), tokens.createAccessToken(7L, "ADMIN"), tokens.createAdminRefreshToken(7L)}) {
            mvc.perform(post("/api/auth/reissue").header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("status").value(401));
        }
        verifyNoInteractions(refreshTokens, users);
    }

    @Test
    void unrecognizedRefreshTokenCannotBeReissued() throws Exception {
        mvc.perform(post("/api/auth/reissue").header("Authorization", bearer(tokens.createRefreshToken(7L))))
                .andExpect(status().isUnauthorized());
        verify(refreshTokens, never()).save(any());
    }

    @Test
    void storedTokenMismatchAndExpiredRefreshTokenAreRejected() throws Exception {
        when(refreshTokens.findByUserId(7L)).thenReturn(Optional.of(
                RefreshToken.builder().userId(7L).refreshToken("revoked-test-token").build()));
        mvc.perform(post("/api/auth/reissue").header("Authorization", bearer(tokens.createRefreshToken(7L))))
                .andExpect(status().isUnauthorized());
        JwtTokenProvider expired = new JwtTokenProvider(TEST_SECRET, 60000, -60000);
        mvc.perform(post("/api/auth/reissue").header("Authorization", bearer(expired.createRefreshToken(7L))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("message").value("만료된 토큰입니다."));
        verify(refreshTokens, never()).save(any());
        verifyNoInteractions(users);
    }

    @Test
    void missingOrMalformedRefreshHeaderIsAnAuthenticationError() throws Exception {
        mvc.perform(post("/api/auth/reissue")).andExpect(status().isUnauthorized());
        for (String header : new String[]{"bad", "Basic value", "Bearer "}) {
            mvc.perform(post("/api/auth/reissue").header("Authorization", header))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("status").value(401));
        }
        verifyNoInteractions(refreshTokens);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "SHELTER_ADMIN"})
    void ownerCanUpdatePostWithDifferentUserAndPostIds(String role) throws Exception {
        Post post = postOwnedBy(7L);
        when(posts.findById(42L)).thenReturn(Optional.of(post));
        when(animals.findByPost_PostId(42L)).thenReturn(Optional.of(mock(PostAnimal.class)));
        mvc.perform(patch("/api/posts/42").header("Authorization", bearer(tokens.createAccessToken(7L, role)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"changed\",\"content\":\"updated body\",\"images\":[]}"))
                .andExpect(status().isOk());
        assertEquals("changed", post.getTitle());
        assertEquals("updated body", post.getContent());
        verify(posts).findById(42L);
        verify(posts, never()).findById(7L);
    }

    @Test
    void nonOwnerCannotUpdatePost() throws Exception {
        Post post = postOwnedBy(8L);
        when(posts.findById(42L)).thenReturn(Optional.of(post));
        mvc.perform(patch("/api/posts/42").header("Authorization", bearer(tokens.createAccessToken(7L, "USER")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"changed\",\"content\":\"body\"}"))
                .andExpect(status().isForbidden());
        assertEquals("original", post.getTitle());
        verifyNoInteractions(animals, images);
    }

    private User user(long id) {
        User user = User.builder().nickname("test-user").role(Role.USER).build();
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    private Post postOwnedBy(long userId) {
        return Post.builder().author(user(userId)).title("original").content("original body").status(Status.AVAILABLE).build();
    }

    private String bearer(String token) { return "Bearer " + token; }
}
