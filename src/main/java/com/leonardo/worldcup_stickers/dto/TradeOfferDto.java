package com.leonardo.worldcup_stickers.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.leonardo.worldcup_stickers.entities.UserTradeOffersEntity;
import com.leonardo.worldcup_stickers.enums.TradeStatusEnum;

public record TradeOfferDto(
    Long id,
    Long proposerId,
    Long receiverId,
    List<Long> requestedStickerIds,
    List<Long> offeredStickerIds,
    TradeStatusEnum status,
    String message,
    LocalDateTime createdAt,
    LocalDateTime respondedAt) {

    public static TradeOfferDto fromEntity(UserTradeOffersEntity entity) {
        return new TradeOfferDto(
                entity.getId(),
                entity.getProposer().getId(),
                entity.getReceiver().getId(),
                entity.getRequestedStickerIds(),
                entity.getOfferedStickerIds(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getCreatedAt(),
                entity.getRespondedAt());
    }
}
