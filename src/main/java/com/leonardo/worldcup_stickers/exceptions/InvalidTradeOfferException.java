package com.leonardo.worldcup_stickers.exceptions;

import org.springframework.http.HttpStatus;

import com.leonardo.worldcup_stickers.config.ApiException;

public class InvalidTradeOfferException extends ApiException {
    public InvalidTradeOfferException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
