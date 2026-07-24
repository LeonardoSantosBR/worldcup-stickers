package com.leonardo.worldcup_stickers.repositories;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leonardo.worldcup_stickers.entities.UserEntity;

public interface UsersRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
