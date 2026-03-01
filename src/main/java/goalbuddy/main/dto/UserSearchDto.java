package goalbuddy.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserSearchDto {
    private String email;
    private String nickname;
    private String profileImageUrl;
}
