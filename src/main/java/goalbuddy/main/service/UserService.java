package goalbuddy.main.service;

import goalbuddy.main.dto.FriendDto;
import goalbuddy.main.dto.UserSearchDto;
import goalbuddy.main.dto.UserSignupDto;
import goalbuddy.main.entity.Friend;
import goalbuddy.main.entity.User;
import goalbuddy.main.repository.FeedLikeRepository;
import goalbuddy.main.repository.FriendRepository;
import goalbuddy.main.repository.GoalRepository;
import goalbuddy.main.repository.UserRepository;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final FriendRepository friendRepository;
    private final EmailService emailService;
    private final GoalRepository goalRepository;
    private final FeedLikeRepository feedLikeRepository;

    // 회원가입 시 추가 검증 로직
    public void validateSignup(UserSignupDto dto, BindingResult bindingResult) {

        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getPasswordCheck())) {
            bindingResult.rejectValue("passwordCheck", "mismatch");
        }

        // 이메일 중복 가입 여부 확인
        if (userRepository.existsByEmail(dto.getEmail())) {
            bindingResult.rejectValue("email", "exists");
        }

        // 닉네임 중복 여부 확인
        if (userRepository.existsByNickname(dto.getNickname())) {
            bindingResult.rejectValue("nickname", "exists");
        }
    }

    // 닉네임 수정
    @Transactional
    public void updateNickname(Long id, String newNickname) {
        // 영속 상태 유지 위해 User를 여기서 불러옴
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 변경하려는 이름이 현재 이름과 동일할 경우
        if (user.getNickname().equals(newNickname)) {
            return;
        }

        // 변경햐려는 이름이 이미 존재하는 경우 (중복 체크)
        if (userRepository.existsByNickname(newNickname)) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }

        user.updateNickname(newNickname);
        userRepository.save(user);
    }

    // 프로필 사진 수정
    @Transactional
    public void updateProfileImage(Long userId, MultipartFile file) {
        // 물리적 파일 저장 및 웹 접근 경로 반환 (/uploads/UUID.ext)
        String imagePath = fileService.uploadProfileImage(file);

        if (imagePath != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            user.updateProfileImage(imagePath);
            userRepository.save(user);
        }
    }

    @Transactional // 데이터 저장 중 에러 발생 시 롤백 위함
    public void register(UserSignupDto dto) {
        // 실제 저장 로직 (엔티티 변환 및 save)
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = User.builder()
                        .nickname(dto.getNickname())
                                .email(dto.getEmail())
                                        .password(encodedPassword)
                                                .build();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<FriendDto> getFriendList(User user) {
        // 내가 팔로우한 전체 친구 조회
        List<Friend> friends = friendRepository.findByFromUser(user);

        // 친구들 정보 DTO로 변환
        return friends.stream()
                .map(f -> {
                    User toUser = f.getToUser();
                    return FriendDto.builder()
                            .id(toUser.getId())
                            .nickname(toUser.getNickname())
                            .profileImageUrl(toUser.getProfileImageUrl())
                            .streak(toUser.getStreak())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFriend(User fromUser, String toUserNickname) {
        User toUser = userRepository.findByNickname(toUserNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (fromUser.getNickname().equals(toUserNickname)) {
            throw new IllegalStateException("자기 자신을 친구로 추가할 수 없습니다.");
        }

        if (friendRepository.existsByFromUserAndToUser(fromUser, toUser) || friendRepository.existsByFromUserAndToUser(toUser, fromUser)) {
            throw new IllegalStateException("이미 추가된 친구입니다.");
        }

        Friend friend = Friend.builder().
                fromUser(fromUser)
                .toUser(toUser)
                .build();

        friendRepository.save(friend);
    }

    @Transactional
    public void deleteFriend(User me, Long userId) {
        // 삭제 대상 찾기
        User toUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 실제 친구 관계인지 확인
        if (!friendRepository.existsByFromUserAndToUser(me, toUser)) {
            throw new IllegalStateException("친구가 아닙니다.");
        }

        // 삭제
        friendRepository.deleteByFromUserAndToUser(me, toUser);
    }

    @Transactional(readOnly = true)
    public List<UserSearchDto> searchUsers(String keyword, User me) {
        // 검색 키워드 포함된 전체 유저 조회
        List<User> searchResults = userRepository.findByNicknameContainingAndIdNot(keyword, me.getId());

        return searchResults.stream()
                .map(user -> UserSearchDto.builder()
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build()
                ).collect(Collectors.toList());

    }

    @Transactional
    public String requestPasswordReset(String email) { // 패스워드 변경 / Forget Password 클릭 시 호출
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String noticeMessage = "패스워드 재설정을 위한 메일이 발송되었습니다.";

        if (user.isLocked()) {
            noticeMessage = "패스워드 재설정 이후 계정 잠금이 자동으로 해제됩니다.";
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(30)); // 토큰 유효시간은 30분

        userRepository.save(user);

        // 이메일 발송
        String resetLink = "http://localhost:8080/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        return noticeMessage;
    }

    @Transactional // HTTPS 사용, 평문은 RAM에 아주 잠시 저장되므로 보안상 문제 없음
    public void resetPassword(String token, String newRawPassword) {
        log.info("Reset Request - Token: {}, Pwd Length: {}", token, newRawPassword.length());
        // 패스워드 유효성 검사, 최소 패스워드 길이 수는 HTML Form에서 minlength로 관리
        String passwordPattern = "^(?=.*[a-zA-Z])(?=.*\\d|.*\\W).{8,}$|^(?=.*\\d)(?=.*\\W).{8,}$";

        if (!newRawPassword.matches(passwordPattern)) {
            throw new IllegalArgumentException("비밀번호는 영어 대/소문자, 숫자, 특수문자 중 2가지 이상을 사용해야 합니다.");
        }

        // 토큰으로 유저 찾기
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 접근입니다."));

        // 유효한 토큰인지 검증 (시간)
        if (!user.isResetTokenValid(token)) {
            throw new IllegalStateException("링크가 만료되었습니다. 다시 요청해 주세요.");
        }

        String encodedPassword = passwordEncoder.encode(newRawPassword);
        user.updatePassword(encodedPassword);
    }

    @Transactional // 회원 탈퇴
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

//        // 참조 무결성 발생하지 않도록 순서에 맞게 삭제해야 함
//
//        // 1. FeedLike 삭제
//        feedLikeRepository.deleteByUser(user); // 내가 누른 좋아요 삭제
//        feedLikeRepository.deleteByGoalIn(goalRepository.findByUser(user)); // 내 글에 눌린 좋아요 삭제
//
//
//        // 친구 관계 삭제
//        friendRepository.deleteByFromUserAndToUser(user, user);
//
//        // 작성한 목표 및 피드 데이터 삭제
//        goalRepository.deleteByUser(user);

        userRepository.delete(user);
    }

}
