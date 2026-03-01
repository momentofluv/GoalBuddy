package goalbuddy.main.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"from_user_id", "to_user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Friend {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id",
            foreignKey = @ForeignKey(name = "fk_friend_from_user",
            foreignKeyDefinition = "FOREIGN KEY (from_user_id) REFERENCES user(id) ON DELETE CASCADE"))
    private User fromUser; // 친구 요청을 보내는 자 = 로그인 된 상태 = 나

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id",
            foreignKey = @ForeignKey(name = "fk_friend_to_user",
                    foreignKeyDefinition = "FOREIGN KEY (to_user_id) REFERENCES user(id) ON DELETE CASCADE"))
    private User toUser;

    @Builder
    public Friend(User fromUser, User toUser) {
        this.fromUser = fromUser;
        this.toUser = toUser;
    }
}
