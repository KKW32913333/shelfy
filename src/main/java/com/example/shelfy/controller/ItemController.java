package com.example.shelfy.controller;

import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ShelfyItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class ItemController {

    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;

    public ItemController(ShelfyItemService itemService, CurrentUserService currentUserService) {
        this.itemService = itemService;
        this.currentUserService = currentUserService;
    }

    // =============================================
    // 日用品一覧
    // =============================================

    @GetMapping("/daily")
    public String dailyList(@RequestParam(required = false) String q,
                            @RequestParam(required = false) String status,
                            Model model) {
        Long groupId = currentUserService.getCurrentGroupId();
        var items = q != null && !q.isBlank()
                ? itemService.search(groupId, q)
                : itemService.getByCategory(groupId, "daily");

        model.addAttribute("items", items);
        model.addAttribute("keyword", q);
        model.addAttribute("status", status);
        model.addAttribute("pageTitle", "日用品管理");
        model.addAttribute("category", "daily");
        return "items/daily-list";
    }

    // =============================================
    // 食品・調味料一覧
    // =============================================

    @GetMapping("/food")
    public String foodList(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String categoryFilter,
                           Model model) {
        Long groupId = currentUserService.getCurrentGroupId();

        var rawItems = (q != null && !q.isBlank())
                ? itemService.search(groupId, q)
                : (categoryFilter != null && !categoryFilter.isBlank())
                    ? itemService.getByCategory(groupId, categoryFilter)
                    : itemService.getAll(groupId).stream()
                        .filter(ShelfyItem::isFood).toList();

        var items = rawItems.stream()
                .sorted((a, b) -> {
                    if (a.getExpiryDate() == null && b.getExpiryDate() == null) return 0;
                    if (a.getExpiryDate() == null) return 1;
                    if (b.getExpiryDate() == null) return -1;
                    return a.getExpiryDate().compareTo(b.getExpiryDate());
                }).toList();

        model.addAttribute("items", items);
        model.addAttribute("keyword", q);
        model.addAttribute("categoryFilter", categoryFilter);
        model.addAttribute("pageTitle", "食品・調味料管理");
        return "items/food-list";
    }

    // =============================================
    // アイテム詳細
    // =============================================

    @GetMapping("/items/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Long groupId = currentUserService.getCurrentGroupId();
        model.addAttribute("item", itemService.getById(id, groupId));
        return "items/detail";
    }

    // =============================================
    // 新規登録フォーム
    // =============================================

    @GetMapping("/items/new")
    public String newForm(@RequestParam(defaultValue = "daily") String category,
                          Model model) {
        ShelfyItem item = new ShelfyItem();
        item.setCategory(category);
        model.addAttribute("item", item);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("isNew", true);
        model.addAttribute("groupId", currentUserService.getCurrentGroupId());
        return "items/edit";
    }

    // =============================================
    // 編集フォーム
    // =============================================

    @GetMapping("/items/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Long groupId = currentUserService.getCurrentGroupId();
        model.addAttribute("item", itemService.getById(id, groupId));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("isNew", false);
        model.addAttribute("groupId", groupId);
        return "items/edit";
    }

    // =============================================
    // 保存（新規・更新共通）
    // =============================================

    @PostMapping("/items/save")
    public String save(@ModelAttribute ShelfyItem item,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) Long groupId,
                       RedirectAttributes ra) {
        Long userId = currentUserService.getCurrentUserId();
        Long resolvedGroupId = groupId != null ? groupId : currentUserService.getOrDetectGroupId(userId);
        System.out.println("SAVE resolvedGroupId=" + resolvedGroupId + " fromForm=" + groupId);
        item.setGroupId(resolvedGroupId);

        boolean isNew = (item.getId() == null);
        if (isNew) item.setCreatedBy(userId);
        item.setUpdatedBy(userId);

        ShelfyItem saved = itemService.save(item);

        if (isNew) {
            ra.addFlashAttribute("success", "登録しました。写真を追加できます");
            return "redirect:/items/" + saved.getId();
        }
        ra.addFlashAttribute("success", "更新しました");
        return "redirect:/" + (item.isDaily() ? "daily" : "food");
    }

    // =============================================
    // 在庫数クイック更新（+1 / -1 ボタン）
    // =============================================

    @PostMapping("/items/{id}/stock")
    public String updateStock(@PathVariable Long id,
                              @RequestParam int delta,
                              @RequestParam(defaultValue = "/") String returnTo,
                              RedirectAttributes ra) {
        Long groupId = currentUserService.getCurrentGroupId();
        Long userId  = currentUserService.getCurrentUserId();
        itemService.updateStock(id, groupId, delta, userId);
        ra.addFlashAttribute("success", "在庫数を更新しました");
        return "redirect:" + returnTo;
    }

    // =============================================
    // 削除
    // =============================================

    @PostMapping("/items/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "/daily") String returnTo,
                         RedirectAttributes ra) {
        Long groupId = currentUserService.getCurrentGroupId();
        itemService.delete(id, groupId);
        ra.addFlashAttribute("success", "削除しました");
        return "redirect:" + returnTo;
    }
}
