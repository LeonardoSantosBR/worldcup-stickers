package com.leonardo.worldcup_stickers.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leonardo.worldcup_stickers.config.JwtAuthFilter;
import com.leonardo.worldcup_stickers.dto.AvailableTradeStickerDto;
import com.leonardo.worldcup_stickers.dto.MakeAvailableTradeDto;
import com.leonardo.worldcup_stickers.dto.PageResponseDto;
import com.leonardo.worldcup_stickers.entities.StickerEntity;
import com.leonardo.worldcup_stickers.services.StickersService;
import com.leonardo.worldcup_stickers.services.UserTradeInventoriesService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/stickers")
public class StickersController {
    private final StickersService stickersService;
    private final UserTradeInventoriesService userTradeInventoriesService;

    public StickersController(
            StickersService stickersService,
            UserTradeInventoriesService userTradeInventoriesService) {
        this.stickersService = stickersService;
        this.userTradeInventoriesService = userTradeInventoriesService;
    }

    @PostMapping("/open-package")
    public List<StickerEntity> OpenPackage(@RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId) {
        return stickersService.openPackage(userId);
    }

    @PostMapping("/make-available-trade")
    public boolean MakeAvailableTrade(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId,
            @Valid @RequestBody MakeAvailableTradeDto body) {
        return userTradeInventoriesService.makeAvailableTrade(userId, body.stickerIds());
    }

    @GetMapping("/available-trades")
    public PageResponseDto<AvailableTradeStickerDto> AvailableTrades(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String name,
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId) {
        return userTradeInventoriesService.findAllAvailableForTrade(userId, page, limit, name);
    }
}
