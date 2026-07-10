package com.example.shelfy.controller;

import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.ShelfyItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;

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

        var items = (q != null && !q.isBlank())
                ? itemService.search(groupId, q)
                : (categoryFilter != null && !categoryFilter.isBlank())
                    ? itemService.getByCategory(groupId, categoryFilter)
                    : itemService.getAll(groupId).stream()
                        .filter(ShelfyItem::isFood).toList();

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
        return "items/edit";
    }

    // =============================================
    // 保存（新規・更新共通）
    // =============================================

    @PostMapping("/items/save")
    public String save(@ModelAttribute ShelfyItem item,
                       RedirectAttributes ra) {
        Long groupId  = currentUserService.getCurrentGroupId();
        Long userId   = currentUserService.getCurrentUserId();
        item.setGroupId(groupId);

        boolean isNew = (item.getId() == null);
        if (isNew) item.setCreatedBy(userId);
        item.setUpdatedBy(userId);

        itemService.save(item);

        ra.addFlashAttribute("success", isNew ? "登録しました" : "更新しました");
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
