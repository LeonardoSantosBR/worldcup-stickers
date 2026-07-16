package com.leonardo.worldcup_stickers.controller;

import org.springframework.web.bind.annotation.RestController;

import com.leonardo.worldcup_stickers.entities.User;
import com.leonardo.worldcup_stickers.services.UsersService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final UsersService usersService;

    public UsersController(UsersService usersService){
        this.usersService = usersService;
    }
    
    @PostMapping
    public boolean create(@RequestBody User user){
        return this.usersService.create(user);
    }
}