package goalbuddy.main.dto;

import goalbuddy.main.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupDto {
    @NotBlank(message = "{required.userSignupDto.email}") @Email
    private String email;

    @NotBlank(message = "{required.userSignupDto.password}")
    @Size(min=8, message = "{size.userSignupDto.password}")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d|.*\\W).{8,}$|^(?=.*\\d)(?=.*\\W).{8,}$",
            message = "{validation.userSignupDto.password}"
    )
    private String password;

    @NotBlank(message = "{required.userSignupDto.passwordCheck}")
    private String passwordCheck;

    @NotBlank(message = "{required.userSignupDto.nickname}")
    @Size(min=2, max=10, message = "{size.userSignupDto.nickname}")
    private String nickname;

}
