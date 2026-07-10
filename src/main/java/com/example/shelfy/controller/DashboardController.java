package com.example.shelfy.controller;

import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ShelfyItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;

    @Qualifier("linkleJdbcTemplate")
    private final JdbcTemplate linkleJdbc;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Long userId = currentUserService.getCurrentUserId();

        // グループIDが未設定の場合、Linkleのグループから自動取得
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
            // Linkleのgroup_membershipからグループIDを取得
            List<Map<String, Object>> rows = linkleJdbc.queryForList(
                "SELECT group_id FROM group_membership WHERE user_id = ? LIMIT 1", userId);
            if (!rows.isEmpty()) {
                return ((Number) rows.get(0).get("group_id")).longValue()
cat > src/main/java/com/example/shelfy/controller/DashboardController.java << 'JAVAEOF'
package com.example.shelfy.controller;

import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ShelfyItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;

    @Qualifier("linkleJdbcTemplate")
    private final JdbcTemplate linkleJdbc;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Long userId = currentUserService.getCurrentUserId();

        // グループIDが未設定の場合、Linkleのグループから自動取得
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
            // Linkleのgroup_membershipからグループIDを取得
            List<Map<String, Object>> rows = linkleJdbc.queryForList(
                "SELECT group_id FROM group_membership WHERE user_id = ? LIMIT 1", userId);
            if (!rows.isEmpty()) {
                return ((Number) rows.get(0).get("group_id")).longValue();
            }
            // group_membershipがなければapp_userのactive_group_idを使用
            List<Map<String, Object>> userRows = linkleJdbc.queryForList(
                "SELECT active_group_id FROM app_user WHERE id = ?", userId);
            if (!userRows.isEmpty() && userRows.get(0).get("active_group_id") != null) {
                return ((Number) userRows.get(0).get("active_group_id")).longValue();
            }
        } catch (Exception e) {
            // テーブル構造が違う場合はnullを返す
        }
        return null;
    }
}
