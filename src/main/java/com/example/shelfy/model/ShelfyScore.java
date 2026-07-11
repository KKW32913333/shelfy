package com.example.shelfy.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shelfy_score")
public class ShelfyScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    @Column(name = "score")
    private int score = 0;
    @Column(name = "total_consumed")
    private int totalConsumed = 0;
    @Column(name = "zero_expire_days")
    private int zeroExpireDays = 0;
    @Column(name = "last_updated")
    private LocalDate lastUpdated;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalConsumed() { return totalConsumed; }
    public void setTotalConsumed(int totalConsumed) { this.totalConsumed = totalConsumed; }
    public int getZeroExpireDays() { return zeroExpireDays; }
    public void setZeroExpireDays(int zeroExpireDays) { this.zeroExpireDays = zeroExpireDays; }
    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
