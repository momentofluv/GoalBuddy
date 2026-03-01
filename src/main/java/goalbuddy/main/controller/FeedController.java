package goalbuddy.main.controller;

import goalbuddy.main.dto.FeedDto;
import goalbuddy.main.dto.LikeRequest;
import goalbuddy.main.dto.LikesResponseDto;
import goalbuddy.main.entity.User;
import goalbuddy.main.repository.GoalRepository;
import goalbuddy.main.repository.LikeRepository;
import goalbuddy.main.repository.UserRepository;
import goalbuddy.main.service.CustomUserDetails;
import goalbuddy.main.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor // 생성자 주입 자동 생성
public class FeedController {

    private final GoalService goalService;
    private final LikeRepository likeRepository;

    @GetMapping("/feed")
    public String feedPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {

        List<FeedDto> feeds = goalService.getFeedList(userDetails.getUser());
        model.addAttribute("feeds", feeds);

        return "feed";
    }

    @PostMapping("/api/likes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody LikeRequest request) {

        // 토글(체크박스) 클릭으로 인한 값 변화 -> 현재 유저의 대상 유저 및 날짜에 대한 좋아요 누른 여부 조회
        boolean isLiked = goalService.toggleFeedLike(
                userDetails.getUser(),
                request.getTargetNickname(),
                LocalDate.now()
        );

        int currentLikes = goalService.getLikeCount(request.getTargetNickname(), LocalDate.now());

        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);
        response.put("likes", currentLikes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/likes/{nickname}/users")
    @ResponseBody
    public ResponseEntity<List<LikesResponseDto>> getLikers(@PathVariable String nickname) {
        LocalDate today = LocalDate.now();

        List<User> likers = likeRepository.findLikersByTargetNicknameAndDate(nickname, today);

        // 엔티티를 DTO로 변환하여 필요한 값만 반환
        List<LikesResponseDto> response = likers.stream()
                .map(user -> new LikesResponseDto(user.getNickname(), user.getProfileImageUrl()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}
