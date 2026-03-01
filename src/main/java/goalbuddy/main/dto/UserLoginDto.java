package goalbuddy.main.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class UserLoginDto {
    @NotBlank(message = "{required.userSignupDto.email}")
    @Email(message = "{email.userLoginDto.email}")
    private String email;

    @NotBlank(message = "{required.userSignupDto.password}")
    private String password;
}
