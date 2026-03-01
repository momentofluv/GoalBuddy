package goalbuddy.main.controller;

import goalbuddy.main.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    public final UserService userService;

    // login form에서 비밀번호 찾기 선택 -> 메일 발송
    @PostMapping("/send-reset-mail") // URL을 명확히 분리!
    @ResponseBody
    public ResponseEntity<String> sendMail(@RequestParam String email) {
        try {
            userService.requestPasswordReset(email); // 토큰 생성 및 메일 발송 로직 실행
        } catch (Exception e) {
            // 가입되지 않은 이메일로 요청 발생 시 로그만 찍기
            log.error("Mail Send Error: {}", e.getMessage());
        }

        return ResponseEntity.ok("가입된 이메일이 입력되었다면 해당 이메일로 재설정 링크가 발송됩니다.");
    }

    // 전송된 메일 링크 클릭 시 비밀번호 재설정 페이지로 이동
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {

        model.addAttribute("token", token);  // 추후 POST 요청 시 함께 전달해야 함
        return "reset-password";
    }

    // 비밀번호 변경 처리
    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<String> realPasswordReset(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword, // Form에서 입력한 데이터
            @RequestParam("confirmPassword") String confirmPassword) {

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("입력된 두 비밀번호가 일치하지 않습니다.");
        }

        try { // 입력된 두 비밀번호 일치해야만 패스워드 재설정 로직 실행
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다. 다시 로그인해 주세요.");
        } catch (IllegalStateException e) {
            // 토큰 만료 등의 경우
            return ResponseEntity.badRequest().body("다시 시도해 주세요.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("패스워드 변경 중 오류가 발생하였습니다.");
        }
    }
}
