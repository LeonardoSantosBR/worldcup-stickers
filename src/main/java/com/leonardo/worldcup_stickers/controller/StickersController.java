package com.leonardo.worldcup_stickers.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leonardo.worldcup_stickers.config.JwtAuthFilter;
import com.leonardo.worldcup_stickers.entities.StickerEntity;
import com.leonardo.worldcup_stickers.services.StickersService;

@RestController
@RequestMapping("/stickers")
public class StickersController {
    private final StickersService stickersService;

    public StickersController(StickersService stickersService) {
        this.stickersService = stickersService;
    }

    @PostMapping("/open-package")
    public List<StickerEntity> openPackage(@RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId) {
        return stickersService.openPackage(userId);
    }
}
