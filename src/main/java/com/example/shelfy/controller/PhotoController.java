package com.example.shelfy.controller;

import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.service.CurrentUserService;
import com.example.shelfy.service.PhotoService;
import com.example.shelfy.service.ShelfyItemService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PhotoController {

    private final PhotoService photoService;
    private final ShelfyItemService itemService;
    private final CurrentUserService currentUserService;

    public PhotoController(PhotoService photoService, ShelfyItemService itemService, CurrentUserService currentUserService) {
        this.photoService = photoService;
        this.itemService = itemService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/items/{id}/photo")
    public String uploadPhoto(@PathVariable Long id,
                              @RequestParam("photo") MultipartFile photo,
                              RedirectAttributes ra) {
        Long groupId = currentUserService.getCurrentGroupId();
        ShelfyItem item = itemService.getById(id, groupId);

        if (!photo.isEmpty()) {
            if (item.getImageUrl() != null) {
                photoService.delete(item.getImageUrl());
            }
            String url = photoService.upload(photo, id);
            item.setImageUrl(url);
            itemService.save(item);
            ra.addFlashAttribute("success", "写真を登録しました");
        }

        return "redirect:/items/" + id;
    }

    @PostMapping("/items/{id}/photo/delete")
    public String deletePhoto(@PathVariable Long id, RedirectAttributes ra) {
        Long groupId = currentUserService.getCurrentGroupId();
        ShelfyItem item = itemService.getById(id, groupId);

        if (item.getImageUrl() != null) {
            photoService.delete(item.getImageUrl());
            item.setImageUrl(null);
            itemService.save(item);
            ra.addFlashAttribute("success", "写真を削除しました");
        }

        return "redirect:/items/" + id;
    }
}
