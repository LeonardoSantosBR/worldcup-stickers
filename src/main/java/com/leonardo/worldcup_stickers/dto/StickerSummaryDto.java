package com.leonardo.worldcup_stickers.dto;

/** Minimal sticker identification for listings that only need to name it. */
public record StickerSummaryDto(
    Long id,
    String name) {
}
