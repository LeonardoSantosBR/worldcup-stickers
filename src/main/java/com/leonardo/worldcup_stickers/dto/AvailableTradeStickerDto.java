package com.leonardo.worldcup_stickers.dto;

public record AvailableTradeStickerDto(
    Long stickerId,
    String stickerName,
    Long ownerId,
    String ownerName,
    String ownerEmail) {

    public static AvailableTradeStickerDto fromView(AvailableTradeStickerView view) {
        return new AvailableTradeStickerDto(
            view.getStickerId(),
            view.getStickerName(),
            view.getOwnerId(),
            view.getOwnerName(),
            view.getOwnerEmail());
    }
}
