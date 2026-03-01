package goalbuddy.main.service;

import goalbuddy.main.dto.FeedDto;
import goalbuddy.main.entity.FeedLike;
import goalbuddy.main.entity.Friend;
import goalbuddy.main.entity.Goal;
import goalbuddy.main.entity.User;
import goalbuddy.main.repository.FeedLikeRepository;
import goalbuddy.main.repository.FriendRepository;
import goalbuddy.main.repository.GoalRepository;
import goalbuddy.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalService {
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FriendRepository friendRepository;

    public List<FeedDto> getFeedList(User currentUser) {

        // 내가 먼저 등록한 친구, 다른 친구가 친구 관계를 등록한 친구 모두 친구로 조회
        List<Friend> allRelationships = friendRepository.findAllFriendsByUser(currentUser);

        // 각 레코드에서 친구 객체 추출 (to/fromUser 모두 확인해야 함 - 내가 toUser면 상대는 from, 반대도 고려)
        Set<User> targets = allRelationships.stream()
                        .map(f -> f.getFromUser().getId().equals(currentUser.getId()) ? f.getToUser() : f.getFromUser())
                        .collect(Collectors.toSet());

        targets.add(currentUser);  // 내 피드도 출력되어야 하므로 출력 타겟에 나도 추가, set이므로 이미 존재 시 무시

        // 날짜 포맷을 미리 정의해서 DTO의 date 필드에 주입
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
        String formattedDate = LocalDate.now().format(formatter); // "Feb 21" 형태로 생성

        return targets.stream().map(user -> {
            List<Goal> userGoals = goalRepository.findByUserAndDate(user, LocalDate.now());

            long completed = userGoals.stream().filter(Goal::isDone).count();
            int total = userGoals.size();
            int achievementRate = (total > 0) ? (int) Math.round((double) completed / total * 100) : 0;

            int likeCount = feedLikeRepository.countByTargetUserAndLikedDate(user, LocalDate.now());
            boolean isLiked = feedLikeRepository.findByUserAndTargetUserAndLikedDate(currentUser, user, LocalDate.now())
                    .isPresent();

            return FeedDto.builder()
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .achievementRate(achievementRate)
                    .completedCount((int) completed)
                    .totalCount(total)
                    .goals(userGoals.stream()
                            .map(g -> FeedDto.GoalItemDto.builder()
                                    .content(g.getContent())
                                    .isDone(g.isDone())
                                    .build())
                            .collect(Collectors.toList()))
                    .likes(likeCount)
                    .isLiked(isLiked)
                    .isMine(user.getId().equals(currentUser.getId())) // ID 비교로 본인 확인
                    .date(formattedDate)
                    .build();
        })
                .sorted((f1, f2) -> Boolean.compare(f2.isMine(), f1.isMine())) // 내 목표가 가장 위에 오도록 정렬
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean toggleFeedLike(User currentUser, String targetNickname, LocalDate date) {

        // 좋아요 설정 / 삭제 (isLiked) - DB 연동

        User targetUser = userRepository.findByNickname(targetNickname)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Optional<FeedLike> existingLike = feedLikeRepository.findByUserAndTargetUserAndLikedDate(currentUser, targetUser, date);

        if (existingLike.isPresent()) { // 이미 좋아요 되어 있다면 좋아요 취소
            feedLikeRepository.delete(existingLike.get());
            feedLikeRepository.flush(); // 즉시 반영
            return false;
        } else {
            FeedLike newLike = FeedLike.builder()
                    .user(currentUser)
                    .targetUser(targetUser)
                    .likedDate(date)
                    .build();
            feedLikeRepository.save(newLike);
            return true;
        }
    }

    public int getLikeCount(String nickname, LocalDate date) {
        // 해당 사용자의 날짜의 피드의 좋아요 개수
        User targetUser = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return feedLikeRepository.countByTargetUserAndLikedDate(targetUser, date);
    }

    @Transactional
    public void updateStreak(Long id, FeedDto myFeed) {

        // 영속성 위해 여기서 User 찾기
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));;

        LocalDate today = LocalDate.now();
        LocalDate lastDate = user.getLastStreakedDate();

        // 어제 streak 달성하지 못했고, 오늘도 streak 달성하지 못한 경우
        // lastDate가 오늘인 경우 제외하지 않으면 오늘 streak 1 처음 달성된 기록까지 삭제될 수 있음
        if (lastDate != null && !lastDate.equals(today.minusDays(1)) && !lastDate.equals(today)) {
            user.setStreak(0);
            user.setLastStreakedDate(null); // 끊긴 기록 초기화
            lastDate = null;
        }

        // 오늘 등록된 목표가 있고 모두 완료했을 때
        if (myFeed.getTotalCount() > 0 && myFeed.getCompletedCount() == myFeed.getTotalCount()) {

            // 오늘 이미 streak 업데이트가 된 경우 streak 중복 카운트 X
            if (today.equals(lastDate)) {
                return;
            }

            // 어제도 streak을 달성하였고, 오늘도 모든 목표를 성공한 경우
            if (lastDate != null && lastDate.equals(today.minusDays(1))) {
                user.setStreak(user.getStreak() + 1);
            } else { // 오늘 streak을 처음 달성한 경우
                user.setStreak(1);
            }
            user.setLastStreakedDate(today);
        }

        // 오늘 등록된 목표를 모두 완수해 streak + 1 되었지만 목표가 추가되어 다시 완수하지 못한 상태가 된 경우
        else if (today.equals(lastDate)) {
            if (user.getStreak() <= 1) {
                user.setStreak(0);
                user.setLastStreakedDate(null);
            } else {
                user.setStreak(user.getStreak() - 1);
                user.setLastStreakedDate(today.minusDays(1));
            }
        }

        userRepository.save(user);
    }

    // 오늘 나의 목표 관련 정보 가져오기
    public FeedDto getMyFeed(User user, LocalDate date) {
        return getFeedList(user).stream()
                .filter(FeedDto::isMine) // getFeedList에서 얻은 정보 중 내 정보만 가져오기
                .findFirst()
                .orElse(null);
    }

}
