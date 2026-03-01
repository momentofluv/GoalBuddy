package goalbuddy.main.repository;

import goalbuddy.main.entity.Friend;
import goalbuddy.main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByFromUser(User fromUser); // 현재 로그인된 사용자의 친구 목록 조회
    boolean existsByFromUserAndToUser(User FromUser, User toUser); // 친구 여부 조회
    @Query("SELECT f FROM Friend f WHERE f.fromUser = :user OR f.toUser = :user")
    List<Friend> findAllFriendsByUser(@Param("user") User user); // 내가 먼저 등록해도 남이 먼저 등록해도 모두 친구!!!

    @Modifying
    @Transactional
    void deleteByFromUserAndToUser(User FromUser, User toUser); // 친구 삭제
}
