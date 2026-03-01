package goalbuddy.main.config.auth;

import goalbuddy.main.entity.User;
import goalbuddy.main.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("email");
        String errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다."; // 기본 메시지

        HttpSession session = request.getSession(true);

        if (exception instanceof BadCredentialsException) { // 비밀번호 틀렸을 때

            Optional<User> nowUser = userRepository.findByEmail(email);

            if (nowUser.isPresent()) {
                User user = nowUser.get();
                boolean isJustLocked = user.addFailCountAndCheck(); // 이번 로그인으로 5회째 실패한 사용자인지 검출
                userRepository.save(user);

                if (isJustLocked) {
                    errorMessage = "로그인 5회 이상 오류로 계정이 잠겼습니다.<br>이메일 인증으로 비밀번호를 재설정해 주세요.";
                } else {
                    errorMessage = "이메일 또는 비밀번호가 올바르지 않습니다.<br>(현재 " + user.getLoginFailCount() + "회 실패)";
                }
            }
        } else if (exception instanceof LockedException) { // 계정이 이미 잠긴 경우
            errorMessage = "로그인 5회 이상 오류로 계정이 잠겼습니다.<br>등록된 이메일로 비밀번호 재설정 시 잠금이 해제됩니다.";
        }

        session.setAttribute("errorMessage", errorMessage);

        getRedirectStrategy().sendRedirect(request, response, "/login?error=true");

    }

}
