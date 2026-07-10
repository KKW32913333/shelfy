package com.example.shelfy.controller;

import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ShelfyItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Long groupId = currentUserService.getCurrentGroupId();

        model.addAttribute("stats",        itemService.getDashboardStats(groupId));
        model.addAttribute("expiringItems",itemService.getExpiringItems(groupId));
        model.addAttribute("lowStockItems",itemService.getLowStockItems(groupId));
        model.addAttribute("recentItems",  itemService.getRecentItems(groupId, 8));
        model.addAttribute("categoryCount",itemService.getCategoryCount(groupId));

        return "dashboard";
    }
}
