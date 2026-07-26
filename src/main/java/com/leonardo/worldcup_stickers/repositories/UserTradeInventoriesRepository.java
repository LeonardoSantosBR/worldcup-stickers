package com.leonardo.worldcup_stickers.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.leonardo.worldcup_stickers.dto.AvailableTradeStickerView;
import com.leonardo.worldcup_stickers.entities.UserTradeInventoryEntity;

public interface UserTradeInventoriesRepository extends JpaRepository<UserTradeInventoryEntity, Long> {
    Optional<UserTradeInventoryEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query(value = """
            SELECT s.id AS "stickerId",
                   s.player_name AS "stickerName",
                   s.rarity,
                   s.position,
                   u.id AS "ownerId",
                   u.name AS "ownerName",
                   u.email AS "ownerEmail"
            FROM user_trade_inventories i
            JOIN users u ON u.id = i.user_id AND u.deleted_at IS NULL
            CROSS JOIN LATERAL unnest(i.available_sticker_ids) AS sticker_id
            JOIN stickers s ON s.id = sticker_id AND s.deleted_at IS NULL
            WHERE i.user_id <> :userId
            ORDER BY s.number ASC, u.name ASC
            """,
            countQuery = """
            SELECT count(*)
            FROM user_trade_inventories i
            JOIN users u ON u.id = i.user_id AND u.deleted_at IS NULL
            CROSS JOIN LATERAL unnest(i.available_sticker_ids) AS sticker_id
            JOIN stickers s ON s.id = sticker_id AND s.deleted_at IS NULL
            WHERE i.user_id <> :userId
            """,
            nativeQuery = true)
    Page<AvailableTradeStickerView> findAllAvailableForTrade(@Param("userId") Long userId, Pageable pageable);
}
