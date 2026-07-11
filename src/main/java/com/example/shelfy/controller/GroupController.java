package com.example.shelfy.controller;

import com.example.shelfy.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;

@Controller
public class GroupController {

    private final JdbcTemplate linkleJdbc;
    private final CurrentUserService currentUserService;

    public GroupController(
            @Qualifier("linkleJdbcTemplate") JdbcTemplate linkleJdbc,
            CurrentUserService currentUserService) {
        this.linkleJdbc = linkleJdbc;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/groups")
    public String groupList(Model model) {
        Long userId = currentUserService.getCurrentUserId();
        String sql = "SELECT g.id, g.name FROM app_group g JOIN group_membership gm ON gm.group_id = g.id WHERE gm.user_id = ? ORDER BY g.name";
        List<Map<String, Object>> groups = linkleJdbc.queryForList(sql, userId);
        model.addAttribute("groups", groups);
        model.addAttribute("currentGroupId", currentUserService.getCurrentGroupId());
        return "groups";
    }

    @PostMapping("/groups/select")
    public String selectGroup(@RequestParam Long groupId, RedirectAttributes ra) {
        Long userId = currentUserService.getCurrentUserId();
        List<Map<String, Object>> check = linkleJdbc.queryForList(
            "SELECT id FROM group_membership WHERE user_id = ? AND group_id = ?",
            userId, groupId);
        if (check.isEmpty()) {
            ra.addFlashAttribute("error", "そのグループには参加していません");
            return "redirect:/groups";
        }
        currentUserService.setCurrentGroupId(groupId);
        ra.addFlashAttribute("success", "グループを切り替えました");
        return "redirect:/dashboard";
    }
}
