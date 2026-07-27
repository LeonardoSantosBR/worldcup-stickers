package com.leonardo.worldcup_stickers.exceptions;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

import com.leonardo.worldcup_stickers.config.ApiException;

public class StickersNotAvailableForTradeException extends ApiException {
    public StickersNotAvailableForTradeException(Long ownerId, Collection<Long> stickerIds) {
        super(HttpStatus.BAD_REQUEST, "Stickers not available for trade from user " + ownerId + ": "
                + stickerIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
    }
}
