package goalbuddy.main.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            // MimeMessage 생성
            MimeMessage message = mailSender.createMimeMessage();

            // Helper를 사용하여 인코딩(UTF-8)과 멀티파트 설정
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail.trim()); // 혹시 모를 공백 제거
            helper.setFrom("diveintoce4n@gmail.com");
            helper.setSubject("[GoalBuddy] 비밀번호 재설정 안내");


            String content = "안녕하세요. 아래 링크를 클릭하여 비밀번호를 재설정하세요.\n" +
                    "비밀번호 재설정 시 잠금 또한 자동으로 해제됩니다.\n\n" +
                    resetLink;

            helper.setText(content, false); // 일반 텍스트 모드
            mailSender.send(message);

        } catch (MessagingException e) {
            // SMTP 전송 실패 시 로그 출력
            throw new RuntimeException("메일 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}