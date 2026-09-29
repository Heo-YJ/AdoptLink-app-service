package animals.demo.security;

import animals.demo.common.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityErrorHandler securityErrorHandler;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 재발급 헤더는 access token이 아닌 refresh token이며 서비스에서 별도로 검증한다.
        return "POST".equals(request.getMethod())
                && (request.getContextPath() + "/api/auth/reissue").equals(request.getRequestURI());
    }

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        String token = resolveToken(request);

        if(token != null) {
            try {
                JwtTokenProvider.AccessTokenIdentity identity = jwtTokenProvider.validateAccessToken(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                identity.userId(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + identity.role()))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (CustomException e) {
                SecurityContextHolder.clearContext();
                securityErrorHandler.writeError(response, e.getErrorCode());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    //요청 헤더에서 토큰을 꺼내는 메서드
    //클라이언트가 요청할 때 헤더에 담아서 보냄
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
