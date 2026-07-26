package com.leonardo.worldcup_stickers.dto;

public interface AvailableTradeStickerView {
    Long getStickerId();

    String getStickerName();

    String getRarity();

    String getPosition();

    Long getOwnerId();

    String getOwnerName();

    String getOwnerEmail();
}
