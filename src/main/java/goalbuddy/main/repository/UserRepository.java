package goalbuddy.main.repository;

import goalbuddy.main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); // 동일 이메일로 가입된 사용자 있는지 확인
    boolean existsByNickname(String nickname);  // 닉네임 중복 여부 확인
    Optional<User> findByEmail(String email);    // 이메일로 사용자 찾기 - DB에서 그대로 가져오는 것이므로 User
    Optional<User> findByNickname(String nickname); // NPE 예방 위해 Optional로 감싸야 함
    List<User> findByNicknameContainingAndIdNot(String nickname, Long myId); // nickname 검색어를 포함하는 모든 사용자 출력 (나는 제외)
    Optional<User> findByResetToken(String token);

    Optional<User> findById(Long id);
}
