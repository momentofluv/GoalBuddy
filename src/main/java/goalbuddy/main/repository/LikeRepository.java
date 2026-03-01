package goalbuddy.main.repository;

import goalbuddy.main.entity.FeedLike;
import goalbuddy.main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LikeRepository extends JpaRepository<FeedLike, Long> {

    @Query("SELECT fl.user FROM FeedLike fl " +
            "WHERE fl.targetUser.nickname = :targetNickname " +
            "AND fl.likedDate = :today")
    List<User> findLikersByTargetNicknameAndDate(
            @Param("targetNickname") String targetNickname,
            @Param("today") LocalDate today);
}

