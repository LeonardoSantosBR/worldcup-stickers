package com.leonardo.worldcup_stickers.dto;

import jakarta.validation.constraints.Size;

public record RespondOfferDto(
    @Size(max = 255, message = "note must be at most 255 characters")
    String note) {
}
