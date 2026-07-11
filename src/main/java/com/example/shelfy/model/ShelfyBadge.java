package com.example.shelfy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shelfy_badge")
public class ShelfyBadge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    @Column(name = "badge_key", length = 50)
    private String badgeKey;
    @Column(name = "badge_name", length = 100)
    private String badgeName;
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getBadgeKey() { return badgeKey; }
    public void setBadgeKey(String badgeKey) { this.badgeKey = badgeKey; }
    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }
}
