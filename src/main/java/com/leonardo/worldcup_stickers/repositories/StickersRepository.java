package com.leonardo.worldcup_stickers.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leonardo.worldcup_stickers.entities.StickerEntity;
import com.leonardo.worldcup_stickers.enums.RarityEnum;

public interface StickersRepository extends JpaRepository<StickerEntity, Long> {
    List<StickerEntity> findByRarity(RarityEnum rarity);
}
