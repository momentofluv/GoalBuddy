package goalbuddy.main.repository;

import goalbuddy.main.entity.Goal;
import goalbuddy.main.entity.FeedLike;
import goalbuddy.main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    // 특정 사용자의 특정 날짜의 피드에 기록된 좋아요 개수 출력
    int countByTargetUserAndLikedDate(User targetUser, LocalDate likedDate);

    // 중복 좋아요 예방 위해 이미 좋아요 눌렀는지 확인
    Optional<FeedLike> findByUserAndTargetUserAndLikedDate(User user, User targetUser, LocalDate likedDate);

    @Transactional
    void deleteByUser(User user);
}
