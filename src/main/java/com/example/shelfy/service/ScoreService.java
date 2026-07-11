package com.example.shelfy.service;

import com.example.shelfy.model.ShelfyBadge;
import com.example.shelfy.model.ShelfyScore;
import com.example.shelfy.repository.ShelfyBadgeRepository;
import com.example.shelfy.repository.ShelfyItemRepository;
import com.example.shelfy.repository.ShelfyScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreService {

    private static final Logger log = LoggerFactory.getLogger(ScoreService.class);

    private final ShelfyScoreRepository scoreRepo;
    private final ShelfyBadgeRepository badgeRepo;
    private final ShelfyItemRepository itemRepo;

    public ScoreService(ShelfyScoreRepository scoreRepo,
                        ShelfyBadgeRepository badgeRepo,
                        ShelfyItemRepository itemRepo) {
        this.scoreRepo = scoreRepo;
        this.badgeRepo = badgeRepo;
        this.itemRepo = itemRepo;
    }

    public ShelfyScore getOrCreate(Long groupId) {
        return scoreRepo.findByGroupId(groupId).orElseGet(() -> {
            ShelfyScore s = new ShelfyScore();
            s.setGroupId(groupId);
            s.setScore(0);
            s.setTotalConsumed(0);
            s.setZeroExpireDays(0);
            s.setLastUpdated(LocalDate.now());
            s.setCreatedAt(LocalDateTime.now());
            return scoreRepo.save(s);
        });
    }

    @Transactional
    public List<ShelfyBadge> addConsumption(Long groupId, int quantity) {
        ShelfyScore score = getOrCreate(groupId);
        score.setTotalConsumed(score.getTotalConsumed() + quantity);
        score.setScore(score.getScore() + quantity * 10);
        score.setLastUpdated(LocalDate.now());
        scoreRepo.save(score);
        return checkAndAwardBadges(groupId, score);
    }

    @Transactional
    public void updateZeroExpireStreak(Long groupId) {
        ShelfyScore score = getOrCreate(groupId);
        long expiredCount = itemRepo.countByGroupIdAndExpiryDateBefore(groupId, LocalDate.now());
        if (expiredCount == 0) {
            score.setZeroExpireDays(score.getZeroExpireDays() + 1);
            score.setScore(score.getScore() + 5);
        } else {
            score.setZeroExpireDays(0);
        }
        score.setLastUpdated(LocalDate.now());
        scoreRepo.save(score);
        checkAndAwardBadges(groupId, score);
    }

    private List<ShelfyBadge> checkAndAwardBadges(Long groupId, ShelfyScore score) {
        List<ShelfyBadge> newBadges = new ArrayList<>();

        List<Object[]> badgeDefs = List.of(
            new Object[]{"consume_10",   "🌱 使い切り10回達成",    score.getTotalConsumed() >= 10},
            new Object[]{"consume_50",   "⭐ 使い切り50回達成",    score.getTotalConsumed() >= 50},
            new Object[]{"consume_100",  "🏆 使い切り100回達成",   score.getTotalConsumed() >= 100},
            new Object[]{"zero_7",       "🥗 期限切れゼロ7日継続",  score.getZeroExpireDays() >= 7},
            new Object[]{"zero_30",      "🎖️ 期限切れゼロ30日継続", score.getZeroExpireDays() >= 30},
            new Object[]{"score_100",    "💫 スコア100点突破",      score.getScore() >= 100},
            new Object[]{"score_500",    "🔥 スコア500点突破",      score.getScore() >= 500},
            new Object[]{"score_1000",   "👑 スコア1000点突破",     score.getScore() >= 1000}
        );

        for (Object[] def : badgeDefs) {
            String key  = (String) def[0];
            String name = (String) def[1];
            boolean cond = (Boolean) def[2];
            if (cond && !badgeRepo.existsByGroupIdAndBadgeKey(groupId, key)) {
                ShelfyBadge badge = new ShelfyBadge();
                badge.setGroupId(groupId);
                badge.setBadgeKey(key);
                badge.setBadgeName(name);
                badge.setEarnedAt(LocalDateTime.now());
                badgeRepo.save(badge);
                newBadges.add(badge);
                log.info("バッジ獲得: {} - {}", groupId, name);
            }
        }
        return newBadges;
    }

    public List<ShelfyBadge> getBadges(Long groupId) {
        return badgeRepo.findByGroupIdOrderByEarnedAtDesc(groupId);
    }
}
