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
public class ShoppingListController {

    private final JdbcTemplate linkleJdbc;
    private final CurrentUserService currentUserService;

    public ShoppingListController(
            @Qualifier("linkleJdbcTemplate") JdbcTemplate linkleJdbc,
            CurrentUserService currentUserService) {
        this.linkleJdbc = linkleJdbc;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/shopping")
    public String shopping(Model model) {
        Long groupId = currentUserService.getCurrentGroupId();
        List<Map<String, Object>> items = linkleJdbc.queryForList(
            "SELECT * FROM shopping_item WHERE group_id = ? ORDER BY sort_order ASC, id DESC",
            groupId);
        model.addAttribute("items", items);
        return "shopping";
    }

    @PostMapping("/shopping/{id}/check")
    public String check(@PathVariable Long id, RedirectAttributes ra) {
        linkleJdbc.update(
            "UPDATE shopping_item SET purchased = NOT purchased WHERE id = ?", id);
        return "redirect:/shopping";
    }

    @PostMapping("/shopping/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        linkleJdbc.update("DELETE FROM shopping_item WHERE id = ?", id);
        ra.addFlashAttribute("success", "削除しました");
        return "redirect:/shopping";
    }
}
