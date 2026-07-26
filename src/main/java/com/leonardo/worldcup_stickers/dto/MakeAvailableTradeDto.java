package com.leonardo.worldcup_stickers.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MakeAvailableTradeDto(
    @NotEmpty(message = "stickerIds must not be empty")
    List<@NotNull @Positive Long> stickerIds) {
}
