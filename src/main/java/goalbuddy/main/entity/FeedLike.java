package goalbuddy.main.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "target_user_id", "liked_Date"}) // 동일 날짜의 피드에 좋아요 두 번 남길 수 없도록 함
})
public class FeedLike { // 특정 날짜 단위로 좋아요 남길 수 있도록 함

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_feedlike_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE"))
    private User user; // 좋아요 누른 사람


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable=false,
            foreignKey = @ForeignKey(name = "fk_feedlike_target_user",
                    foreignKeyDefinition = "FOREIGN KEY (target_user_id) REFERENCES user(id) ON DELETE CASCADE"))
    private User targetUser; // 좋아요 받은 피드의 주인

    private LocalDate likedDate; // 어떤 날짜의 피드인지 기록

    @Builder
    public FeedLike(User user, User targetUser, LocalDate likedDate) {
        this.user = user;
        this.targetUser = targetUser;
        this.likedDate = likedDate;
    }
}
