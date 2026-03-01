package goalbuddy.main.service;

import goalbuddy.main.entity.User;
import goalbuddy.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    // DB에서 해당 사용자의 패스워드 불러오는 역할 (USerLoginDto에 담긴 값과 비교할 수 있도록)


    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB에서 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다."));

        // 스프링 시큐리티 전용 User 객체 생성해 DB에서 가져온 정보 매핑
        return new CustomUserDetails(user);
    }
}
