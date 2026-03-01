package goalbuddy.main.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
// 삭제 시 SecurityConfig도 수정

@Profile("local")
public class DevAutoLoginFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    public DevAutoLoginFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 이미 인증된 상태라면 통과
// 1. 이미 인증되었거나, 정적 리소스 요청이면 통과
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            String testEmail = "example@example.com";
            UserDetails userDetails = userDetailsService.loadUserByUsername(testEmail);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // [중요] 세션에도 인증 정보를 명시적으로 저장해줍니다. (이게 빠지면 다음 필터에서 또 로그인하라고 함)
            request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        }

        // 2. 현재 요청이 /login 이라면, 이미 인증됐으니 /today로 리다이렉트 시켜버립니다.
        if (request.getRequestURI().equals("/login") && SecurityContextHolder.getContext().getAuthentication() != null) {
            response.sendRedirect("/today");
            return; // 필터 체인 중단
        }

        filterChain.doFilter(request, response);
    }
}
