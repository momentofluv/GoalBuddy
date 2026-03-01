package goalbuddy.main.config;

import goalbuddy.main.config.auth.LoginFailureHandler;
import goalbuddy.main.security.DevAutoLoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginFailureHandler loginFailureHandler;
    // private final DevAutoLoginFilter devAutoLoginFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/auth/**", "/login", "/signup") // 인증 관련 경로는 CSRF 검사 제외
            )
            .authorizeHttpRequests(auth -> auth
                // CSS, JS, 이미지 및 회원가입 페이지는 로그인 없이 접근 허용
                .requestMatchers("/signup", "/css/**", "/js/**", "/image/**", "/login", "/uploads/**", "/auth/**", "/error").permitAll()
                // 그 외 모든 요청(홈 화면 포함)은 로그인이 필요함
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")       // 우리가 만든 로그인 페이지 경로
                .loginProcessingUrl("/login") // 로그인 폼의 action 경로
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/today", true)   // 로그인 성공 시 이동할 홈 경로
                .failureUrl("/login?error")
                .failureHandler(loginFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true) // 세션 삭제
                .deleteCookies("JSESSIONID") // 쿠키 삭제
            );

        return http.build();
    }

    // 정적 리소스 보안 필터에서 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/css/**", "/js/**", "/image/**", "/favicon.ico");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호를 안전하게 암호화하기 위한 빈 등록
        return new BCryptPasswordEncoder();
    }
}