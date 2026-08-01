package com.leonardo.worldcup_stickers.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.leonardo.worldcup_stickers.entities.UserTradeOffersEntity;
import com.leonardo.worldcup_stickers.enums.TradeStatusEnum;

public record TradeOfferDetailDto(
    Long id,
    Long proposerId,
    String proposerName,
    Long receiverId,
    List<StickerSummaryDto> requestedStickers,
    List<StickerSummaryDto> offeredStickers,
    TradeStatusEnum status,
    String message,
    LocalDateTime createdAt,
    LocalDateTime respondedAt) {

    public static TradeOfferDetailDto fromEntity(UserTradeOffersEntity entity, Map<Long, String> stickerNames) {
        return new TradeOfferDetailDto(
                entity.getId(),
                entity.getProposer().getId(),
                entity.getProposer().getName(),
                entity.getReceiver().getId(),
                toSummaries(entity.getRequestedStickerIds(), stickerNames),
                toSummaries(entity.getOfferedStickerIds(), stickerNames),
                entity.getStatus(),
                entity.getMessage(),
                entity.getCreatedAt(),
                entity.getRespondedAt());
    }

    private static List<StickerSummaryDto> toSummaries(List<Long> stickerIds, Map<Long, String> stickerNames) {
        return stickerIds.stream()
                .map(id -> new StickerSummaryDto(id, stickerNames.get(id)))
                .toList();
    }
}
