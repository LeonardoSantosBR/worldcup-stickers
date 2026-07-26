package com.leonardo.worldcup_stickers.dto;

/**
 * Projection for the native query that unnests user_trade_inventories.available_sticker_ids.
 * Aliases in the query are quoted so Postgres keeps the camelCase exactly as declared here.
 */
public interface AvailableTradeStickerView {
    Long getStickerId();

    String getStickerName();

    Long getOwnerId();

    String getOwnerName();

    String getOwnerEmail();
}
