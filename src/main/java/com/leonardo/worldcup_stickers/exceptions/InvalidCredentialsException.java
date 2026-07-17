package com.leonardo.worldcup_stickers.exceptions;

import org.springframework.http.HttpStatus;

import com.leonardo.worldcup_stickers.config.ApiException;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
