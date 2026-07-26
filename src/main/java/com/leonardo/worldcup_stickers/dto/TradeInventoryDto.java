package com.leonardo.worldcup_stickers.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.leonardo.worldcup_stickers.entities.UserTradeInventoryEntity;

public record TradeInventoryDto(
    Long userId,
    List<Long> availableStickerIds,
    LocalDateTime updatedAt) {

    public static TradeInventoryDto fromEntity(UserTradeInventoryEntity inventory) {
        return new TradeInventoryDto(
            inventory.getUser().getId(),
            List.copyOf(inventory.getAvailableStickerIds()),
            inventory.getUpdatedAt());
    }
}
