package com.example.shelfy.controller;

import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.repository.ShelfyItemRepository;
import com.example.shelfy.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PhotoListController {

    private final ShelfyItemRepository itemRepository;
    private final CurrentUserService currentUserService;

    @GetMapping("/photos")
    public String photos(@RequestParam(required = false) String category, Model model) {
        Long groupId = currentUserService.getCurrentGroupId();

        List<ShelfyItem> items = (category != null && !category.isBlank())
            ? itemRepository.findByGroupIdAndCategoryOrderByUpdatedAtDesc(groupId, category)
            : itemRepository.findByGroupIdOrderByUpdatedAtDesc(groupId);

        model.addAttribute("items", items);
        model.addAttribute("category", category);
        return "photos";
    }
}
