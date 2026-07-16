package com.leonardo.worldcup_stickers.exceptions;

import org.springframework.http.HttpStatus;

import com.leonardo.worldcup_stickers.config.ApiException;

public class EmailAlreadyExistsException extends ApiException {
     public EmailAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "User with this email already exists: " + email);
    }
}
