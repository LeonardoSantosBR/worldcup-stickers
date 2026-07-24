package com.leonardo.worldcup_stickers.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.leonardo.worldcup_stickers.entities.UserStickerEntity;

public interface UserStickersRepository extends JpaRepository<UserStickerEntity, Long> {
    Optional<UserStickerEntity> findByUserIdAndStickerId(Long userId, Long stickerId);

    @EntityGraph(attributePaths = "sticker")
    Page<UserStickerEntity> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
