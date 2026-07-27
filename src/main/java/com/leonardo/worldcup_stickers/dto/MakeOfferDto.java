package com.leonardo.worldcup_stickers.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MakeOfferDto(
    @NotNull(message = "receiverId must not be null")
    @Positive
    Long receiverId,

    @NotEmpty(message = "requestedStickerIds must not be empty")
    List<@NotNull @Positive Long> requestedStickerIds,

    @NotEmpty(message = "offeredStickerIds must not be empty")
    List<@NotNull @Positive Long> offeredStickerIds,

    @Size(max = 255, message = "message must be at most 255 characters")
    String message) {
}
