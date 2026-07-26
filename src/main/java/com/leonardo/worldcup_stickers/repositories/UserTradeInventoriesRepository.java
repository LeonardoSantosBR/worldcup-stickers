package com.leonardo.worldcup_stickers.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leonardo.worldcup_stickers.entities.UserTradeInventoryEntity;

public interface UserTradeInventoriesRepository extends JpaRepository<UserTradeInventoryEntity, Long> {
    Optional<UserTradeInventoryEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
