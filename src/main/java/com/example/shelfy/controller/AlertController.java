package com.example.shelfy.controller;

import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.repository.ShelfyItemRepository;
import com.example.shelfy.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AlertController {

    private final ShelfyItemRepository itemRepository;
    private final CurrentUserService currentUserService;

    public AlertController(ShelfyItemRepository itemRepository, CurrentUserService currentUserService) {
        this.itemRepository = itemRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/alerts")
    public String alerts(Model model) {
        Long groupId = currentUserService.getCurrentGroupId();
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);
        LocalDate in30Days = today.plusDays(30);

        List<ShelfyItem> allItems = itemRepository.findByGroupIdOrderByUpdatedAtDesc(groupId);

        List<ShelfyItem> expiredItems = allItems.stream()
            .filter(i -> i.getExpiryDate() != null && i.getExpiryDate().isBefore(today))
            .sorted((a, b) -> a.getExpiryDate().compareTo(b.getExpiryDate()))
            .collect(Collectors.toList());

        List<ShelfyItem> soonItems = allItems.stream()
            .filter(i -> i.getExpiryDate() != null
                && !i.getExpiryDate().isBefore(today)
                && !i.getExpiryDate().isAfter(in7Days))
            .sorted((a, b) -> a.getExpiryDate().compareTo(b.getExpiryDate()))
            .collect(Collectors.toList());

        List<ShelfyItem> lowStockItems = itemRepository.findLowStockItems(groupId);

        model.addAttribute("expiredItems", expiredItems);
        model.addAttribute("soonItems", soonItems);
        model.addAttribute("lowStockItems", lowStockItems);

        return "alerts";
    }
}
