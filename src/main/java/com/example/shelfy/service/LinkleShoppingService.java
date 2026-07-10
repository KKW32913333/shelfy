package com.example.shelfy.service;

import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.repository.ShelfyItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Linkle買い物リスト連携サービス
 *
 * 在庫が0になった商品をLinkleのshopping_itemテーブルへ自動登録する。
 * 重複登録を防ぐため linkedToShopping フラグで管理。
 */
@Service
public class LinkleShoppingService {

    private static final Logger log = LoggerFactory.getLogger(LinkleShoppingService.class);

    private final JdbcTemplate linkleJdbc;
    private final ShelfyItemRepository itemRepository;

    public LinkleShoppingService(
            @Qualifier("linkleJdbcTemplate") JdbcTemplate linkleJdbc,
            ShelfyItemRepository itemRepository) {
        this.linkleJdbc = linkleJdbc;
        this.itemRepository = itemRepository;
    }

    // =============================================
    // 在庫0になったタイミングで即時連携
    // =============================================

    /**
     * 在庫更新時に呼び出す。
     * stock が 0 以下になった場合にLinkleへ追加。
     */
    @Transactional
    public void syncIfEmpty(ShelfyItem item) {
        if (item.getStock() <= 0 && !item.isLinkedToShopping()) {
            addToLinkle(item);
        }
    }

    // =============================================
    // 定期チェック（毎時0分）
    // 手動で在庫を変更した場合などの取りこぼし対策
    // =============================================

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Tokyo")
    public void scheduledSync() {
        // グループIDは全グループを対象にチェック
        List<ShelfyItem> targets = itemRepository.findAll().stream()
                .filter(i -> i.getStock() <= 0 && !i.isLinkedToShopping())
                .toList();

        if (!targets.isEmpty()) {
            log.info("Linkle連携対象: {}件", targets.size());
            targets.forEach(this::addToLinkle);
        }
    }

    // =============================================
    // Linkle shopping_item への INSERT
    // =============================================

    private void addToLinkle(ShelfyItem item) {
        try {
            /*
             * Linkleのshopping_itemテーブル構造：
             * id, group_id, name, quantity, memo, checked,
             * created_by, sort_order, source
             *
             * source = 'shelfy' で手動追加と区別できる
             */
            String sql = """
                INSERT INTO shopping_item
                  (name, quantity, memo, purchased, created_by, updated_by, created_at, updated_at, group_id, sort_order)
                VALUES
                  (?, 1, ?, false, ?, ?, NOW(), NOW(), ?, COALESCE((SELECT MAX(sort_order)+1 FROM shopping_item WHERE group_id = ?), 1))
                """;

            String memo = buildMemo(item);

            linkleJdbc.update(sql,
                    item.getName(),
                    memo,
                    item.getCreatedBy(),
                    item.getCreatedBy(),
                    item.getGroupId(),
                    item.getGroupId()
            );

            // フラグ更新（Shelfy DB）
            item.setLinkedToShopping(true);
            item.setLinkedAt(LocalDateTime.now());
            itemRepository.save(item);

            log.info("Linkle買い物リストに追加: {} (groupId={})", item.getName(), item.getGroupId());

        } catch (Exception e) {
            log.error("Linkle連携失敗: {} - {}", item.getName(), e.getMessage());
            // 連携失敗しても在庫管理の動作は継続
        }
    }

    /**
     * 買い物リストのメモ欄に表示するテキストを生成
     * 例：「【Shelfy】食品 / 在庫切れ」
     */
    private String buildMemo(ShelfyItem item) {
        String categoryLabel = switch (item.getCategory()) {
            case "food"      -> "食品";
            case "seasoning" -> "調味料";
            case "drink"     -> "飲料";
            case "daily"     -> "日用品";
            default          -> "その他";
        };
        return "【Shelfy自動追加】" + categoryLabel;
    }

    // =============================================
    // 手動で連携リセット（在庫を補充したとき）
    // =============================================

    /**
     * 在庫が補充されたらLinkleの該当アイテムを購入済みにする（オプション）
     * Linkle側のcheckedをtrueに更新する
     */
    @Transactional
    public void markAsCheckedInLinkle(ShelfyItem item) {
        try {
            String sql = """
                UPDATE shopping_item
                SET checked = true
                WHERE group_id = ? AND name = ? AND source = 'shelfy' AND checked = false
                """;
            int updated = linkleJdbc.update(sql, item.getGroupId(), item.getName());
            if (updated > 0) {
                item.setLinkedToShopping(false); // 次回在庫切れで再連携できるようにリセット
                itemRepository.save(item);
                log.info("Linkle購入済みに更新: {}", item.getName());
            }
        } catch (Exception e) {
            log.error("Linkle購入済み更新失敗: {}", e.getMessage());
        }
    }
}
