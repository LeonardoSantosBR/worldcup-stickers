package com.leonardo.worldcup_stickers.exceptions;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

import com.leonardo.worldcup_stickers.config.ApiException;

public class StickersNotOwnedException extends ApiException {
    public StickersNotOwnedException(Collection<Long> stickerIds) {
        super(HttpStatus.BAD_REQUEST, "Stickers not owned by user: " + stickerIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")));
    }
}
