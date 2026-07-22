package com.leonardo.worldcup_stickers.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leonardo.worldcup_stickers.entities.UserSticker;

public interface UserStickersRepository extends JpaRepository<UserSticker, Long> {
    Optional<UserSticker> findByUserIdAndStickerId(Long userId, Long stickerId);
}
