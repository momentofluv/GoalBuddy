package goalbuddy.main.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 등록된 목표의 id

    private String content;
    private boolean isDone;
    private LocalDate date; // 목표 등록 날짜

    public void updateDone(boolean newDone) {
        this.isDone = newDone;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }

    @ManyToOne(fetch = FetchType.LAZY) // N:1 mapping
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_goal_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE"))
    private User user;
}
