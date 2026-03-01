package goalbuddy.main.controller;

import goalbuddy.main.dto.UserLoginDto;
import goalbuddy.main.dto.UserSignupDto;
import goalbuddy.main.entity.User;
import goalbuddy.main.service.CustomUserDetails;
import goalbuddy.main.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            HttpServletRequest request, Model model) {

        // 이 빈 객체에 사용자에게서 입력받은 로그인 정보 주입
        model.addAttribute("userLoginDto", new UserLoginDto());

        // 로그인 실패 오류 처리
        if (error != null) {
            // LoginFailureHandler가 세션에 저장한 메시지 꺼내기
            HttpSession session = request.getSession(false);

            if (session != null) {
                String errorMessage = (String) session.getAttribute("errorMessage");
                model.addAttribute("errorMessage", errorMessage);

                // session.removeAttribute("errorMessage"); // 데이터 무결성 위해 사용한 메시지 세션에서 제거
            }
        }

        return "login";
    }

    @GetMapping("/signup") 
    public String signupPage(Model model) {
        // 타임리프에서 th:object 사용하기 위해 빈 객체 만들어 넘겨주어야 함
        model.addAttribute("userSignupDto", new UserSignupDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute UserSignupDto userSignupDto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        userService.validateSignup(userSignupDto, bindingResult);

        // DTO 유효성 검사 (입력값 유효한지 확인)
        if (bindingResult.hasErrors()) {
            return "signup"; // 오류 메시지 signup 뷰로 자동 전달
        }

        // DB에 저장
        userService.register(userSignupDto);

        redirectAttributes.addFlashAttribute("signupSuccess", true);
        redirectAttributes.addFlashAttribute("welcomeMessage", "GoalBuddy의 회원이 되신 것을 환영합니다!");
        return "redirect:/login"; // 회원가입 성공 시 로그인 페이지로 이동
    }

    @PostMapping("/withdraw")
    public String withdraw(@AuthenticationPrincipal CustomUserDetails userDetails,
                           HttpServletRequest request, HttpServletResponse response) {

        // DB 데이터 삭제
        userService.withdraw(userDetails.getUser().getId());

        // 로그아웃 처리 (세션 무효화 및 인증 객체 삭제)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/login?withdraw=true"; // 탈퇴 완료 메시지를 위한 파라미터
    }
}
