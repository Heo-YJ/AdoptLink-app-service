package animals.demo.security;

import animals.demo.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.util.Date;

//JWT 토큰을 생성하고 검증하는 핵심 클래스
@Component
public class JwtTokenProvider {

    //JWT 서명에 사용되는 키. 토큰 위변조 판단. 서버만 알고 있어야 함
    private final SecretKey secretKey;
    //토큰 만료 시간
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        //문자열 -> JWT 서명용 Key 객체로 반환
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    //Access Token 생성
    public String createAccessToken(Long userId, String role) {
        //JWT 생성 시작
        return Jwts.builder()
                //토큰 '주인': userId
                .subject(String.valueOf(userId))
                .claim("role", role) //private claim(추가 정보: 권한)
                .issuedAt(new Date()) //발행 시간 설정
                //만료 시간 설정
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey) //서명 설정
                .compact(); //JWT 문자열로 변환
    }

    //Refresh Token 생성
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    //토큰에서 userId 추출
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    //토큰에서 role 추출
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    //토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /*
    핵심 메서드(Claims 파싱)
    1. 서명 검증(secretKey)
    2. 토큰 구조 검증
    3. payload(내용) 추출
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
