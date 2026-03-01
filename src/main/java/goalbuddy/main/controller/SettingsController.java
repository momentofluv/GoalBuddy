package goalbuddy.main.controller;

import goalbuddy.main.entity.User;
import goalbuddy.main.service.CustomUserDetails;
import goalbuddy.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/settings")
@RequiredArgsConstructor
@Controller
public class SettingsController { // 데이터 수정 담당 (닉네임/프로필 사진)

    private final UserService userService;

    @PostMapping("/update-nickname")
    @ResponseBody
    public ResponseEntity<String> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody String newNickname) {

        String cleanNickname = newNickname.replace("\"", "").trim();

        // 1. 유효성 검사 (공백 등)
        if (cleanNickname == null || cleanNickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("변경할 닉네임을 입력하세요.");
        }

        try {
            userService.updateNickname(userDetails.getUser().getId(), cleanNickname);

            // SecurityContext 인증 객체 재등록 > 즉시 새 닉네임 출력되도록 함
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 현재 인증 정보

            // 기존 userDetails 정보 가져와 닉네임 업데이트
            // principal: 현재 로그인된 사용자 == userDetails에 담긴 값과 동일
            // 정석 보안을 딸려면 이렇게 새 인증 토큰 받는 게 좋다고 함
            CustomUserDetails currentPrincipal = (CustomUserDetails) authentication.getPrincipal();
            currentPrincipal.getUser().setNickname(cleanNickname);

            // 변경된 정보 바탕으로 새 인증 토큰 생성
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    currentPrincipal,
                    authentication.getCredentials(),
                    currentPrincipal.getAuthorities()
            );

            // 새로운 인증 토큰 저장
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("닉네임 변경 중 오류가 발생하였습니다.");
        }

    }

    @PostMapping("/update-profile-image")
    public ResponseEntity<String> updateProfileImage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @RequestParam("profileImage") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("업로드할 프로필 사진이 없습니다.");
        }

        try {
            User user = userDetails.getUser();
            // 물리적 파일 저장 및 사진 로딩 시 참조할 웹 접근 경로 반환 (/uploads/UUID.ext)
            userService.updateProfileImage(user.getId(), file);
            return ResponseEntity.ok("프로필 사진이 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("프로필 사진 변경 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/send-reset-link")
    @ResponseBody
    public ResponseEntity<String> sendResetLinkInMyPage(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 세션에서 현재 로그인한 유저의 이메일 추출
            String email = userDetails.getUsername();

            // 패스워드 변경 요청 메일 발송
            userService.requestPasswordReset(email);
            return ResponseEntity.ok("재설정 메일이 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("메일 발송 중 오류가 발생했습니다.");
        }
    }
}
