package com.example.shelfy.controller;

import com.example.shelfy.model.ShelfyBadge;
import com.example.shelfy.model.ShelfyScore;
import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ScoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ScoreController {

    private final ScoreService scoreService;
    private final CurrentUserService currentUserService;

    public ScoreController(ScoreService scoreService, CurrentUserService currentUserService) {
        this.scoreService = scoreService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/score")
    public String score(Model model) {
        Long groupId = currentUserService.getCurrentGroupId();
        ShelfyScore score = scoreService.getOrCreate(groupId);
        List<ShelfyBadge> badges = scoreService.getBadges(groupId);
        List<String> earnedKeys = badges.stream()
            .map(ShelfyBadge::getBadgeKey)
            .collect(Collectors.toList());

        List<Object[]> badgeDefs = List.of(
            new Object[]{"consume_10",  "使い切り10回達成",    "🌱"},
            new Object[]{"consume_50",  "使い切り50回達成",    "⭐"},
            new Object[]{"consume_100", "使い切り100回達成",   "🏆"},
            new Object[]{"zero_7",      "期限切れゼロ7日継続",  "🥗"},
            new Object[]{"zero_30",     "期限切れゼロ30日継続", "🎖️"},
            new Object[]{"score_100",   "スコア100点突破",      "💫"},
            new Object[]{"score_500",   "スコア500点突破",      "🔥"},
            new Object[]{"score_1000",  "スコア1000点突破",     "👑"}
        );

        model.addAttribute("score", score);
        model.addAttribute("badges", badges);
        model.addAttribute("earnedKeys", earnedKeys);
        model.addAttribute("badgeDefs", badgeDefs);
        return "score";
    }
}
