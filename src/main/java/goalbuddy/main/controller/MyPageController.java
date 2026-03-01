package goalbuddy.main.controller;

import goalbuddy.main.dto.FeedDto;
import goalbuddy.main.dto.FriendDto;
import goalbuddy.main.entity.User;
import goalbuddy.main.service.CustomUserDetails;
import goalbuddy.main.service.GoalService;
import goalbuddy.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final GoalService goalService;
    private final UserService userService;

    @GetMapping("/me")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User user = userDetails.getUser();
        model.addAttribute("user", user); // 현재 유저 정보 담기

        FeedDto myFeed = goalService.getMyFeed(user, LocalDate.now());
        model.addAttribute("myFeed", myFeed); // 현재 유저의 목표 정보 담기

        goalService.updateStreak(user.getId(), myFeed); // streak 업데이트

        List<FriendDto> friendList = userService.getFriendList(user);
        model.addAttribute("friends", friendList);

        return "me_new";
    }

}
