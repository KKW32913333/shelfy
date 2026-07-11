package com.example.shelfy.controller;

import com.example.shelfy.model.ConsumptionLog;
import com.example.shelfy.model.ShelfyBadge;
import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.repository.ConsumptionLogRepository;
import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ScoreService;
import com.example.shelfy.service.ShelfyItemService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class ConsumptionController {

    private final ConsumptionLogRepository consumptionRepo;
    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;
    private final ScoreService scoreService;

    public ConsumptionController(ConsumptionLogRepository consumptionRepo,
                                  ShelfyItemService itemService,
                                  CurrentUserService currentUserService,
                                  ScoreService scoreService) {
        this.consumptionRepo = consumptionRepo;
        this.itemService = itemService;
        this.currentUserService = currentUserService;
        this.scoreService = scoreService;
    }

    @PostMapping("/items/{id}/consume")
    public String consume(@PathVariable Long id,
                          @RequestParam(defaultValue = "1") int quantity,
                          @RequestParam(defaultValue = "/") String returnTo,
                          RedirectAttributes ra) {
        Long groupId = currentUserService.getCurrentGroupId();
        Long userId  = currentUserService.getCurrentUserId();
        ShelfyItem item = itemService.getById(id, groupId);

        ConsumptionLog log = new ConsumptionLog();
        log.setItemId(id);
        log.setGroupId(groupId);
        log.setItemName(item.getName());
        log.setCategory(item.getCategory());
        log.setQuantity(quantity);
        log.setConsumedBy(userId);
        consumptionRepo.save(log);

        int newStock = Math.max(0, item.getStock() - quantity);
        item.setStock(newStock);
        item.setUpdatedBy(userId);
        itemService.save(item);

        List<ShelfyBadge> newBadges = scoreService.addConsumption(groupId, quantity);
        String msg = item.getName() + "を使い切りました (+10pt)";
        if (!newBadges.isEmpty()) {
            msg += " 🎉 " + newBadges.get(0).getBadgeName() + " を獲得！";
        }
        ra.addFlashAttribute("success", msg);
        return "redirect:" + returnTo;
    }

    @GetMapping("/consumption")
    public String consumptionLog(Model model) {
        Long groupId = currentUserService.getCurrentGroupId();
        var logs = consumptionRepo.findByGroupIdOrderByConsumedAtDesc(
            groupId, PageRequest.of(0, 50));
        model.addAttribute("logs", logs);
        return "consumption";
    }
}
