package com.leonardo.worldcup_stickers.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leonardo.worldcup_stickers.entities.Sticker;
import com.leonardo.worldcup_stickers.entities.User;
import com.leonardo.worldcup_stickers.entities.UserSticker;
import com.leonardo.worldcup_stickers.enums.Rarity;
import com.leonardo.worldcup_stickers.exceptions.UserNotFoundException;
import com.leonardo.worldcup_stickers.repositories.StickersRepository;
import com.leonardo.worldcup_stickers.repositories.UserStickersRepository;
import com.leonardo.worldcup_stickers.repositories.UsersRepository;

@Service
public class StickersService {
    private static final int PACKAGE_SIZE = 7;
    private static final int COMMON_CHANCE = 55;
    private static final int RARE_CHANCE = 30;

    private final StickersRepository stickersRepository;
    private final UserStickersRepository userStickersRepository;
    private final UsersRepository usersRepository;
    private final Random random = new Random();

    public StickersService(
            StickersRepository stickersRepository,
            UserStickersRepository userStickersRepository,
            UsersRepository usersRepository) {
        this.stickersRepository = stickersRepository;
        this.userStickersRepository = userStickersRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public List<Sticker> openPackage(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<Sticker> drawnStickers = new ArrayList<>();
        for (int i = 0; i < PACKAGE_SIZE; i++) {
            Rarity rarity = drawRarity();
            Sticker sticker = drawStickerByRarity(rarity);
            addToUserCollection(user, sticker);
            drawnStickers.add(sticker);
        }
        return drawnStickers;
    }

    private Rarity drawRarity() {
        int roll = random.nextInt(100);
        if (roll < COMMON_CHANCE) {
            return Rarity.COMMON;
        }
        if (roll < COMMON_CHANCE + RARE_CHANCE) {
            return Rarity.RARE;
        }
        return Rarity.LEGENDARY;
    }

    private Sticker drawStickerByRarity(Rarity rarity) {
        List<Sticker> pool = stickersRepository.findByRarity(rarity);
        return pool.get(random.nextInt(pool.size()));
    }

    private void addToUserCollection(User user, Sticker sticker) {
        UserSticker userSticker = userStickersRepository
                .findByUserIdAndStickerId(user.getId(), sticker.getId())
                .orElseGet(() -> UserSticker.builder()
                        .user(user)
                        .sticker(sticker)
                        .quantity(0)
                        .build());
        userSticker.setQuantity(userSticker.getQuantity() + 1);
        userStickersRepository.save(userSticker);
    }
}
