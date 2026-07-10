package com.example.shelfy.controller;

import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ShelfyItemService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;
    private final JdbcTemplate linkleJdbc;

    public DashboardController(ShelfyItemService itemService,
                               CurrentUserService currentUserService,
                               @Qualifier("linkleJdbcTemplate") JdbcTemplate linkleJdbc) {
        this.itemService = itemService;
        this.currentUserService = currentUserService;
        this.linkleJdbc = linkleJdbc;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Long userId = currentUserService.getCurrentUserId();
        Long groupId;
        try {
            groupId = currentUserService.getCurrentGroupId();
        } catch (IllegalStateException e) {
            groupId = getFirstGroupId(userId);
            if (groupId == null) {
                model.addAttribute("error", "グループに参加していません。Linkleでグループに参加してください。");
                return "dashboard-no-group";
            }
            currentUserService.setCurrentGroupId(groupId);
        }
        model.addAttribute("stats",         itemService.getDashboardStats(groupId));
        model.addAttribute("expiringItems", itemService.getExpiringItems(groupId));
        model.addAttribute("lowStockItems", itemService.getLowStockItems(groupId));
        model.addAttribute("recentItems",   itemService.getRecentItems(groupId, 8));
        model.addAttribute("categoryCount", itemService.getCategoryCount(groupId));
        return "dashboard";
    }

    private Long getFirstGroupId(Long userId) {
        try {
            List<Map<String, Object>> rows = linkleJdbc.queryForList(
                "SELECT group_id FROM group_membership WHERE user_id = ? LIMIT 1", userId);
            if (rows.size() > 0) {
                return ((Number) rows.get(0).get("group_id")).longValue();
            }
            List<Map<String, Object>> userRows = linkleJdbc.queryForList(
                "SELECT active_group_id FROM app_user WHERE id = ?", userId);
            if (userRows.size() > 0 && userRows.get(0).get("active_group_id") != null) {
                return ((Number) userRows.get(0).get("active_group_id")).longValue();
            }
        } catch (Exception e) {
            // テーブル構造が違う場合はnullを返す
        }
        return null;
    }
}
