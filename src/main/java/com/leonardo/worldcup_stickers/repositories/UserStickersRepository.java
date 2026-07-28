package com.leonardo.worldcup_stickers.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.leonardo.worldcup_stickers.entities.UserStickerEntity;

public interface UserStickersRepository extends JpaRepository<UserStickerEntity, Long> {
    Optional<UserStickerEntity> findByUserIdAndStickerId(Long userId, Long stickerId);

    @Query("SELECT us.sticker.id FROM UserStickerEntity us WHERE us.user.id = :userId")
    List<Long> findStickerIdsByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = "sticker")
    Page<UserStickerEntity> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "sticker")
    Page<UserStickerEntity> findByUserIdAndStickerIdIn(Long userId, Collection<Long> stickerIds, Pageable pageable);

    long countByUserId(Long userId);
}
