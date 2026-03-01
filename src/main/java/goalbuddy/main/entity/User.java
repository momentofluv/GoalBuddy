package goalbuddy.main.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 서버 내에서 사용자 구분 위해 사용
    private Long id;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private int streak = 0;

    @Column(nullable = false)
    private String profileImageUrl = "/image/default-profile.png";

    private int loginFailCount = 0; // 로그인 실패 횟수
    private boolean isLocked = false; // 계정 잠금 여부 (기본값이 false)

    private LocalDate lastStreakedDate; // 마지막으로 streak 기록한 날짜

    // 비밀번호 재설정 시 사용
    private String resetToken;
    private LocalDateTime tokenExpiryDate;

    @Builder
    public User(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.streak = 0; // 기본값 초기화
    }

    public boolean addFailCountAndCheck() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.lock();
            return true; // 방금 계정이 잠겼음을 알려줄 수 있음
        }
        return false;
    }

    public void lock() {
        this.isLocked = true;
    }

    public void resetFailCount() {
        this.loginFailCount = 0; // 로그인 성공 시 리셋
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isResetTokenValid(String token) {  // 토큰 유효성 확인 > 토큰은 패스워드 리셋 선택 시 생성됨
        return token.equals(this.resetToken) &&
                tokenExpiryDate != null &&
                tokenExpiryDate.isAfter(LocalDateTime.now());

    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.resetToken = null;
        this.tokenExpiryDate = null;
        this.isLocked = false;
    }
}
