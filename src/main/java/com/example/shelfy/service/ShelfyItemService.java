package com.example.shelfy.service;

import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.repository.ShelfyItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ShelfyItemService {

    private final ShelfyItemRepository itemRepository;
    private final LinkleShoppingService linkleShoppingService;

    public ShelfyItemService(ShelfyItemRepository itemRepository,
                             LinkleShoppingService linkleShoppingService) {
        this.itemRepository = itemRepository;
        this.linkleShoppingService = linkleShoppingService;
    }

    // =============================================
    // 取得
    // =============================================

    public List<ShelfyItem> getAll(Long groupId) {
        return itemRepository.findByGroupIdOrderByUpdatedAtDesc(groupId);
    }

    public List<ShelfyItem> getByCategory(Long groupId, String category) {
        return itemRepository.findByGroupIdAndCategoryOrderByUpdatedAtDesc(groupId, category);
    }

    public ShelfyItem getById(Long id, Long groupId) {
        return itemRepository.findByIdAndGroupId(id, groupId)
                .orElseThrow(() -> new IllegalArgumentException("アイテムが見つかりません"));
    }

    public List<ShelfyItem> search(Long groupId, String keyword) {
        if (keyword == null || keyword.isBlank()) return getAll(groupId);
        return itemRepository.searchByName(groupId, keyword.trim());
    }

    public List<ShelfyItem> getRecentItems(Long groupId, int limit) {
        return itemRepository.findRecentItems(groupId, PageRequest.of(0, limit));
    }

    // =============================================
    // 登録・更新
    // =============================================

    @Transactional
    public ShelfyItem save(ShelfyItem item) {
        boolean wasLinked = item.isLinkedToShopping();
        ShelfyItem saved = itemRepository.save(item);

        // 在庫が0になったらLinkleへ自動連携
        linkleShoppingService.syncIfEmpty(saved);

        // 在庫が復活したらLinkleの買い物リストを購入済みにする
        if (wasLinked && saved.getStock() > 0) {
            linkleShoppingService.markAsCheckedInLinkle(saved);
        }

        return saved;
    }

    @Transactional
    public ShelfyItem updateStock(Long id, Long groupId, int delta, Long userId) {
        ShelfyItem item = getById(id, groupId);
        int newStock = Math.max(0, item.getStock() + delta);
        item.setStock(newStock);
        item.setUpdatedBy(userId);
        return save(item);
    }

    // =============================================
    // 削除
    // =============================================

    @Transactional
    public void delete(Long id, Long groupId) {
        ShelfyItem item = getById(id, groupId);
        itemRepository.delete(item);
    }

    // =============================================
    // ダッシュボード用集計
    // =============================================

    public DashboardStats getDashboardStats(Long groupId) {
        LocalDate today = LocalDate.now();
        LocalDate in7Days  = today.plusDays(7);
        LocalDate in30Days = today.plusDays(30);

        long total    = itemRepository.countByGroupId(groupId);
        long expired  = itemRepository.countByGroupIdAndExpiryDateBefore(groupId, today);
        long soon     = itemRepository.countExpiringSoon(groupId, today, in7Days);
        long warning  = itemRepository.countExpiringWarning(groupId, in7Days, in30Days);
        long lowStock = itemRepository.findLowStockItems(groupId).size();

        return new DashboardStats(total, expired, soon, warning, lowStock);
    }

    public List<ShelfyItem> getExpiringItems(Long groupId) {
        return itemRepository.findExpiringItems(groupId, LocalDate.now().plusDays(30));
    }

    public List<ShelfyItem> getLowStockItems(Long groupId) {
        return itemRepository.findLowStockItems(groupId);
    }

    // =============================================
    // カテゴリ別件数（ドーナツグラフ用）
    // =============================================

    public Map<String, Long> getCategoryCount(Long groupId) {
        List<ShelfyItem> all = getAll(groupId);
        return Map.of(
            "food",      all.stream().filter(i -> "food".equals(i.getCategory())).count(),
            "seasoning", all.stream().filter(i -> "seasoning".equals(i.getCategory())).count(),
            "drink",     all.stream().filter(i -> "drink".equals(i.getCategory())).count(),
            "daily",     all.stream().filter(i -> "daily".equals(i.getCategory())).count(),
            "other",     all.stream().filter(i -> "other".equals(i.getCategory())).count()
        );
    }

    // =============================================
    // ダッシュボード統計データクラス
    // =============================================

    public record DashboardStats(
        long total,
        long expired,
        long expiringSoon,
        long expiringWarning,
        long lowStock
    ) {}
}
