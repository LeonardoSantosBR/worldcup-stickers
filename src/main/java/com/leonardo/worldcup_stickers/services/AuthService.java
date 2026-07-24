package com.leonardo.worldcup_stickers.services;

import org.springframework.stereotype.Service;

import com.leonardo.worldcup_stickers.entities.UserEntity;
import com.leonardo.worldcup_stickers.exceptions.InvalidCredentialsException;
import com.leonardo.worldcup_stickers.repositories.UsersRepository;

@Service
public class AuthService {
    private final UsersRepository usersRepository;
    private final HashService hashService;
    private final JwtService jwtService;

    public AuthService(UsersRepository usersRepository, HashService hashService, JwtService jwtService) {
        this.usersRepository = usersRepository;
        this.hashService = hashService;
        this.jwtService = jwtService;
    }

    public String signin(String email, String password) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!hashService.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.generateToken(user.getId(), user.getEmail());
    }
}
