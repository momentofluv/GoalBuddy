package goalbuddy.main.dto;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendDto {
    private Long id;                    // 상세 페이지 이동용 ID
    private String nickname;            // 표시할 이름
    private String profileImageUrl;     // 프로필 이미지 경로
    private int streak;                 // 친구의 현재 연속 달성 일수
}
