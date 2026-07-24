package com.leonardo.worldcup_stickers.dto;

import com.leonardo.worldcup_stickers.entities.UserStickerEntity;
import com.leonardo.worldcup_stickers.enums.PositionEnum;
import com.leonardo.worldcup_stickers.enums.RarityEnum;
import com.leonardo.worldcup_stickers.enums.StickerGroupEnum;

public record MyStickerDto(
    Long stickerId,
    Integer number,
    String playerName,
    String country,
    StickerGroupEnum group,
    PositionEnum position,
    RarityEnum rarity,
    String imageUrl,
    Integer quantity) {

    public static MyStickerDto fromEntity(UserStickerEntity userSticker) {
        var sticker = userSticker.getSticker();
        return new MyStickerDto(
            sticker.getId(),
            sticker.getNumber(),
            sticker.getPlayerName(),
            sticker.getCountry(),
            sticker.getGroup(),
            sticker.getPosition(),
            sticker.getRarity(),
            sticker.getImageUrl(),
            userSticker.getQuantity());
    }
}
