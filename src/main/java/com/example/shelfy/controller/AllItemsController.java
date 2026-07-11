package com.example.shelfy.controller;

import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ShelfyItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AllItemsController {

    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;

    public AllItemsController(ShelfyItemService itemService,
                               CurrentUserService currentUserService) {
        this.itemService = itemService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/items")
    public String allItems(@RequestParam(required = false) String q, Model model) {
        Long groupId = currentUserService.getCurrentGroupId();
        var items = (q != null && !q.isBlank())
            ? itemService.search(groupId, q)
            : itemService.getAll(groupId);
        model.addAttribute("items", items);
        model.addAttribute("keyword", q);
        model.addAttribute("total", items.size());
        return "items/all-list";
    }
}
