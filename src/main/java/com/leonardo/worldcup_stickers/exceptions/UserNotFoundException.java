package com.leonardo.worldcup_stickers.exceptions;

import org.springframework.http.HttpStatus;

import com.leonardo.worldcup_stickers.config.ApiException;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(Long userId) {
        super(HttpStatus.NOT_FOUND, "User not found: " + userId);
    }
}
