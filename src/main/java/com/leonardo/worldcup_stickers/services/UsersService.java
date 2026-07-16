package com.leonardo.worldcup_stickers.services;

import org.springframework.stereotype.Service;

import com.leonardo.worldcup_stickers.entities.User;
import com.leonardo.worldcup_stickers.exceptions.EmailAlreadyExistsException;
import com.leonardo.worldcup_stickers.repositories.UsersRepository;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final HashService hashService;

    public UsersService(UsersRepository usersRepository, HashService hashService) {
        this.usersRepository = usersRepository;
        this.hashService = hashService;
    }

    public boolean create(User user) {
        if (usersRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
        String hashedPassword = hashService.hash(user.getPassword());
        user.setPassword(hashedPassword);
        usersRepository.save(user);
        return true;
    }
}
