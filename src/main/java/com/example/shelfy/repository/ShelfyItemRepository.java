package com.example.shelfy.repository;

import com.example.shelfy.model.ShelfyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShelfyItemRepository extends JpaRepository<ShelfyItem, Long> {

    // =============================================
    // 基本取得
    // =============================================

    List<ShelfyItem> findByGroupIdOrderByUpdatedAtDesc(Long groupId);

    List<ShelfyItem> findByGroupIdAndCategoryOrderByUpdatedAtDesc(Long groupId, String category);

    Optional<ShelfyItem> findByIdAndGroupId(Long id, Long groupId);

    // =============================================
    // ダッシュボード用カウント
    // =============================================

    long countByGroupId(Long groupId);

    /** 期限切れ（期限日が今日より前） */
    long countByGroupIdAndExpiryDateBefore(Long groupId, LocalDate date);

    /** 7日以内に期限が来るもの（期限切れは除く） */
    @Query("""
        SELECT COUNT(i) FROM ShelfyItem i
        WHERE i.groupId = :groupId
          AND i.expiryDate >= :today
          AND i.expiryDate <= :sevenDays
    """)
    long countExpiringSoon(
        @Param("groupId") Long groupId,
        @Param("today") LocalDate today,
        @Param("sevenDays") LocalDate sevenDays
    );

    /** 30日以内に期限が来るもの（7日超） */
    @Query("""
        SELECT COUNT(i) FROM ShelfyItem i
        WHERE i.groupId = :groupId
          AND i.expiryDate > :sevenDays
          AND i.expiryDate <= :thirtyDays
    """)
    long countExpiringWarning(
        @Param("groupId") Long groupId,
        @Param("sevenDays") LocalDate sevenDays,
        @Param("thirtyDays") LocalDate thirtyDays
    );

    // =============================================
    // アラート・期限管理
    // =============================================

    /** 期限が近い食品一覧（30日以内、期限切れ含む）並び順：期限日昇順 */
    @Query("""
        SELECT i FROM ShelfyItem i
        WHERE i.groupId = :groupId
          AND i.expiryDate IS NOT NULL
          AND i.expiryDate <= :thirtyDays
        ORDER BY i.expiryDate ASC
    """)
    List<ShelfyItem> findExpiringItems(
        @Param("groupId") Long groupId,
        @Param("thirtyDays") LocalDate thirtyDays
    );

    /** 在庫が0または補充目安を下回るもの */
    @Query("""
        SELECT i FROM ShelfyItem i
        WHERE i.groupId = :groupId
          AND i.stock <= 0
        ORDER BY i.updatedAt DESC
    """)
    List<ShelfyItem> findLowStockItems(@Param("groupId") Long groupId);

    // =============================================
    // Linkle連携（在庫0 かつ 未連携）
    // =============================================

    @Query("""
        SELECT i FROM ShelfyItem i
        WHERE i.groupId = :groupId
          AND i.stock <= 0
          AND i.linkedToShopping = false
    """)
    List<ShelfyItem> findItemsToSyncToLinkle(@Param("groupId") Long groupId);

    // =============================================
    // 検索
    // =============================================

    @Query("""
        SELECT i FROM ShelfyItem i
        WHERE i.groupId = :groupId
          AND LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY i.updatedAt DESC
    """)
    List<ShelfyItem> searchByName(
        @Param("groupId") Long groupId,
        @Param("keyword") String keyword
    );

    // =============================================
    // 最近追加したアイテム（ダッシュボード表示用）
    // =============================================

    @Query("""
        SELECT i FROM ShelfyItem i
        WHERE i.groupId = :groupId
        ORDER BY i.createdAt DESC
    """)
    List<ShelfyItem> findRecentItems(@Param("groupId") Long groupId,
                                      org.springframework.data.domain.Pageable pageable);
}
