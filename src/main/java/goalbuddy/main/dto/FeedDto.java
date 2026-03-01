package goalbuddy.main.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedDto {
    private String nickname; // 작성자
    private String profileImageUrl; // 프로필 사진 경로

    private int achievementRate; // 목표 달성률
    private int completedCount;
    private int totalCount;

    private List<GoalItemDto> goals; // 오늘의 목표들 리스트 (여러 명의 것 출력해야 하니까 미리 저장해 두기)
    private boolean isMine;
    private int likes; // 좋아요 개수
    private boolean isLiked;

    private String date;

    @Getter
    @Setter
    @Builder
    public static class GoalItemDto { // isDone 여부 함께 전달하기 위해 목표 담는 ItemDto 별도 생성
        private String content;
        private boolean isDone;
    }
}
