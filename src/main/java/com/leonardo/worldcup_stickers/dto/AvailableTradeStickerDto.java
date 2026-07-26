package com.leonardo.worldcup_stickers.dto;

import com.leonardo.worldcup_stickers.enums.PositionEnum;
import com.leonardo.worldcup_stickers.enums.RarityEnum;

public record AvailableTradeStickerDto(
    Long stickerId,
    String stickerName,
    RarityEnum rarity,
    PositionEnum position,
    Long ownerId,
    String ownerName,
    String ownerEmail) {

    public static AvailableTradeStickerDto fromView(AvailableTradeStickerView view) {
        return new AvailableTradeStickerDto(
            view.getStickerId(),
            view.getStickerName(),
            RarityEnum.valueOf(view.getRarity()),
            PositionEnum.valueOf(view.getPosition()),
            view.getOwnerId(),
            view.getOwnerName(),
            view.getOwnerEmail());
    }
}
