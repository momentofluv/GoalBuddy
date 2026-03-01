package goalbuddy.main.repository;


import goalbuddy.main.dto.FeedDto;
import goalbuddy.main.entity.Goal;
import goalbuddy.main.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserAndDate(User user, LocalDate date);
    List<Goal> findByUser(User user);
    long countByUserAndDate(User user, LocalDate date);

    @Transactional
    void deleteByUser(User user);


}
