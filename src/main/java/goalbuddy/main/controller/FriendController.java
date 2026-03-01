package goalbuddy.main.controller;

import goalbuddy.main.dto.UserSearchDto;
import goalbuddy.main.entity.User;
import goalbuddy.main.repository.UserRepository;
import goalbuddy.main.service.CustomUserDetails;
import goalbuddy.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController                     // JSON 반환
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDto>> searchUsers(
            @RequestParam String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 나를 제외한 키워드 포함하는 닉네임의 유저 전체 조회
        List<UserSearchDto> results = userService.searchUsers(keyword, userDetails.getUser());

        return ResponseEntity.ok(results);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addFriend(
            @RequestBody String toUserNickname,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            String cleanNickname = toUserNickname.replace("\"", "").trim();
            userService.addFriend(userDetails.getUser(), cleanNickname);
            return ResponseEntity.ok("친구 추가 성공!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("친구 추가 과정에서 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<String> removeFriend(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            userService.deleteFriend(userDetails.getUser(), userId);
            return ResponseEntity.ok("삭제 완료!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("친구 삭제 과정에서 오류가 발생했습니다.");
        }
    }


}
