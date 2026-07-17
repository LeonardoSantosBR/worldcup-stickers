package com.leonardo.worldcup_stickers.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leonardo.worldcup_stickers.dto.AuthDto;
import com.leonardo.worldcup_stickers.dto.TokenResponse;
import com.leonardo.worldcup_stickers.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public TokenResponse signin(@RequestBody AuthDto body) {
        String token = authService.signin(body.getEmail(), body.getPassword());
        return new TokenResponse(token);
    }
}
