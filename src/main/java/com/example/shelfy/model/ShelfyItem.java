package com.example.shelfy.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Shelfy商品エンティティ
 * 日用品・食品・調味料を統合管理
 */
@Entity
@Table(name = "shelfy_item")
public class ShelfyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Linkleのgroup_idと共通 */
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    /** 商品名 */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * カテゴリ
     * daily    = 日用品
     * food     = 食品
     * seasoning= 調味料
     * drink    = 飲料
     * other    = その他
     */
    @Column(nullable = false, length = 20)
    private String category = "daily";

    /** 在庫数 */
    @Column(nullable = false)
    private int stock = 0;

    /** 補充目安（この数を下回るとアラート） */
    @Column(name = "min_stock", nullable = false)
    private int minStock = 1;

    /** メモ */
    @Column(length = 500)
    private String memo;

    /** 購入日 */
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    // =============================================
    // 食品・調味料用 期限情報
    // =============================================

    /**
     * 期限の種別
     * best_before = 賞味期限
     * use_by      = 消費期限
     * null        = 期限なし（日用品など）
     */
    @Column(name = "expiry_type", length = 20)
    private String expiryType;

    /** 期限日 */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // =============================================
    // 写真
    // =============================================

    /** メイン写真URL（Cloudflare R2） */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // =============================================
    // Linkle連携
    // =============================================

    /** Linkleの買い物リストに追加済みか */
    @Column(name = "linked_to_shopping", nullable = false)
    private boolean linkedToShopping = false;

    /** 最後にLinkle連携した日時 */
    @Column(name = "linked_at")
    private LocalDateTime linkedAt;

    // =============================================
    // 管理情報
    // =============================================

    /** 登録者（Linkleのuser_id） */
    @Column(name = "created_by")
    private Long createdBy;

    /** 最終更新者 */
    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =============================================
    // 表示用ヘルパー（永続化しない）
    // =============================================

    /** 期限までの残り日数（null = 期限なし） */
    @Transient
    public Long getDaysUntilExpiry() {
        if (expiryDate == null) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * 期限ステータス
     * expired = 期限切れ（赤）
     * soon    = 7日以内（赤〜橙）
     * warning = 30日以内（黄）
     * ok      = 30日超（緑）
     * none    = 期限なし
     */
    @Transient
    public String getExpiryStatus() {
        Long days = getDaysUntilExpiry();
        if (days == null) return "none";
        if (days < 0)  return "expired";
        if (days <= 7) return "soon";
        if (days <= 30) return "warning";
        return "ok";
    }

    /**
     * 在庫ステータス
     * empty    = 0個（赤）
     * low      = minStock未満（橙）
     * ok       = 十分（緑）
     */
    @Transient
    public String getStockStatus() {
        if (stock <= 0) return "empty";
        if (stock < minStock) return "low";
        return "ok";
    }

    /** 日用品かどうか */
    @Transient
    public boolean isDaily() {
        return "daily".equals(category);
    }

    /** 食品・調味料・飲料かどうか（期限管理対象） */
    @Transient
    public boolean isFood() {
        return "food".equals(category)
                || "seasoning".equals(category)
                || "drink".equals(category);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getExpiryType() { return expiryType; }
    public void setExpiryType(String expiryType) { this.expiryType = expiryType; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isLinkedToShopping() { return linkedToShopping; }
    public void setLinkedToShopping(boolean linkedToShopping) { this.linkedToShopping = linkedToShopping; }
    public LocalDateTime getLinkedAt() { return linkedAt; }
    public void setLinkedAt(LocalDateTime linkedAt) { this.linkedAt = linkedAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}