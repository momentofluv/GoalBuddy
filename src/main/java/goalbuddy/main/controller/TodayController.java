package goalbuddy.main.controller;
import goalbuddy.main.entity.Goal;
import goalbuddy.main.entity.User;
import goalbuddy.main.repository.GoalRepository;
import goalbuddy.main.repository.UserRepository;
import goalbuddy.main.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TodayController {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    @GetMapping("/today")
    public String todayPage(@RequestParam(value = "date", required = false) String dateStr,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
        // userDetails 객체에는 현재 접속한 사용자의 정보 담겨 있음
        User user = userDetails.getUser();

        // 로그인 성공 시 failCount reset
        if (user.getLoginFailCount() > 0 && user.getLoginFailCount() < 5) {
            user.resetFailCount();
            userRepository.save(user);
        }

        LocalDate targetDate; // 쿼리 파라미터로 날짜 전송 안 되면 오늘 날짜 출력
        try {
            targetDate = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
        } catch (Exception e) {
            targetDate = LocalDate.now(); // 날짜 형식 잘못된 경우 오늘 날짜 출력
        }

        // targetDate 날짜가 오늘이 아니면 readOnly
        boolean isReadOnly = !targetDate.equals(LocalDate.now());

        List<Goal> goals = goalRepository.findByUserAndDate(user, targetDate);

        long doneCount = goals.stream().filter(Goal::isDone).count();
        long totalCount = goals.size();

        model.addAttribute("doneCount", doneCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("goals", goals);
        model.addAttribute("isReadOnly", isReadOnly); // HTML에서 활용할 변수
        model.addAttribute("selectedDate", targetDate); // 달력 표시용

        // 화면에 필요한 데이터 전달은 Model model import해 사용

        return "today";
    }

    @PostMapping("/api/goals")
    @ResponseBody
    public ResponseEntity<?> addGoal(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody Map<String, String> payload) {
        User user = userDetails.getUser();
        String content = payload.get("content");
        LocalDate today = LocalDate.now();

        // 목표 등록 개수 제한 (최대 3개)
        if (goalRepository.countByUserAndDate(user, today) >= 3) {
            return ResponseEntity.badRequest().body("하루에 등록 가능한 목표는 최대 3개입니다.");
        }

        // 목표 저장
        Goal goal = Goal.builder()
                .content(content)
                .user(user)
                .isDone(false)
                .date(today)
                .build();
        goalRepository.save(goal);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/goals/{id}")
    @ResponseBody
    public ResponseEntity<?> editGoal(@PathVariable Long id,
                                      @RequestBody Map<String, String> payload,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        // 작성자의 요청인지 확인 (로그인된 작성자와 해당 goal의 주인이 같은지 비교)
        if (!goal.getUser().getId().equals(userDetails.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        goal.updateContent(payload.get("content"));  // 수정된 목표 저장
        goalRepository.save(goal);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/goals/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteGoal(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        // 작성자의 요청인지 확인 (로그인된 작성자와 해당 goal의 주인이 같은지 비교)
        if (!goal.getUser().getId().equals(userDetails.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        goalRepository.delete(goal); // DB에서 목표 삭제

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/goals/{id}/done")
    @ResponseBody
    public ResponseEntity<?> toggleGoal(@PathVariable Long id,
                                        @RequestBody Map<String, Boolean> payload,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 목표입니다."));

        // 작성자의 요청인지 확인 (로그인된 작성자와 해당 goal의 주인이 같은지 비교)
        if (!goal.getUser().getId().equals(userDetails.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        goal.updateDone(payload.get("isDone"));
        goalRepository.save(goal);

        return ResponseEntity.ok().build();
    }
}