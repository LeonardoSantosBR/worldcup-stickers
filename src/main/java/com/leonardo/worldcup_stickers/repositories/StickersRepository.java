package com.leonardo.worldcup_stickers.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.leonardo.worldcup_stickers.entities.Sticker;
import com.leonardo.worldcup_stickers.enums.Rarity;

public interface StickersRepository extends JpaRepository<Sticker, Long> {
    List<Sticker> findByRarity(Rarity rarity);
}
