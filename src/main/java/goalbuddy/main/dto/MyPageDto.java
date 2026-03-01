package goalbuddy.main.dto;

import java.util.List;

public class MyPageDto {
    private String nickname;
    private String email;
    private String profileImageUrl;
    private int achievementRate;
    private int completedCount;
    private int totalCount;
    private int currentStreak;
    private List<FriendDto> friends; // 친구 목록
}
