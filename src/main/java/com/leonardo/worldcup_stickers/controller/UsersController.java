package com.leonardo.worldcup_stickers.controller;

import org.springframework.web.bind.annotation.RestController;

import com.leonardo.worldcup_stickers.config.JwtAuthFilter;
import com.leonardo.worldcup_stickers.dto.MyStickerDto;
import com.leonardo.worldcup_stickers.dto.PageResponseDto;
import com.leonardo.worldcup_stickers.entities.UserEntity;
import com.leonardo.worldcup_stickers.services.UsersService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/users")
public class UsersController {
    private final UsersService usersService;

    public UsersController(UsersService usersService){
        this.usersService = usersService;
    }
    
    @PostMapping
    public boolean create(@RequestBody UserEntity user){
        return this.usersService.create(user);
    }

    @GetMapping("/my-stickers")
    public PageResponseDto<MyStickerDto> myStickers(
            @RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return this.usersService.findMyStickers(userId, page, limit);
    }
    
}