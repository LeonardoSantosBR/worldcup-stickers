package com.leonardo.worldcup_stickers.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leonardo.worldcup_stickers.entities.UserStickerEntity;

public interface UserStickersRepository extends JpaRepository<UserStickerEntity, Long> {
    Optional<UserStickerEntity> findByUserIdAndStickerId(Long userId, Long stickerId);
}
