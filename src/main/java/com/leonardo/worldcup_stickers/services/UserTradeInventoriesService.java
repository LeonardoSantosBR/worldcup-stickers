package com.leonardo.worldcup_stickers.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leonardo.worldcup_stickers.dto.AvailableTradeStickerDto;
import com.leonardo.worldcup_stickers.dto.AvailableTradeStickerView;
import com.leonardo.worldcup_stickers.dto.PageResponseDto;
import com.leonardo.worldcup_stickers.dto.TradeInventoryDto;
import com.leonardo.worldcup_stickers.entities.UserTradeInventoryEntity;
import com.leonardo.worldcup_stickers.exceptions.StickersNotOwnedException;
import com.leonardo.worldcup_stickers.exceptions.UserNotFoundException;
import com.leonardo.worldcup_stickers.repositories.UserStickersRepository;
import com.leonardo.worldcup_stickers.repositories.UserTradeInventoriesRepository;
import com.leonardo.worldcup_stickers.repositories.UsersRepository;

@Service
public class UserTradeInventoriesService {
    private static final int MAX_LIMIT = 100;

    private final UserTradeInventoriesRepository userTradeInventoriesRepository;
    private final UserStickersRepository userStickersRepository;
    private final UsersRepository usersRepository;

    public UserTradeInventoriesService(
            UserTradeInventoriesRepository userTradeInventoriesRepository,
            UserStickersRepository userStickersRepository,
            UsersRepository usersRepository) {
        this.userTradeInventoriesRepository = userTradeInventoriesRepository;
        this.userStickersRepository = userStickersRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public boolean makeAvailableTrade(Long userId, List<Long> stickerIds) {
        Set<Long> requested = new LinkedHashSet<>(stickerIds);
        Set<Long> owned = new HashSet<>(userStickersRepository.findStickerIdsByUserId(userId));

        Set<Long> notOwned = new LinkedHashSet<>(requested);
        notOwned.removeAll(owned);
        if (!notOwned.isEmpty()) {
            throw new StickersNotOwnedException(notOwned);
        }

        UserTradeInventoryEntity inventory = loadOrCreateInventory(userId);
        inventory.setAvailableStickerIds(new ArrayList<>(requested));

        TradeInventoryDto.fromEntity(userTradeInventoriesRepository.save(inventory));
        return true;
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AvailableTradeStickerDto> findAllAvailableForTrade(Long userId, int page, int limit, String name) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.min(Math.max(limit, 1), MAX_LIMIT));

        String nameFilter = (name == null || name.isBlank()) ? null : name.trim();

        Page<AvailableTradeStickerView> result = userTradeInventoriesRepository.findAllAvailableForTrade(userId, nameFilter, pageable);
        return PageResponseDto.from(result, AvailableTradeStickerDto::fromView);
    }

    private UserTradeInventoryEntity loadOrCreateInventory(Long userId) {
        return userTradeInventoriesRepository.findByUserId(userId)
                .orElseGet(() -> UserTradeInventoryEntity.builder()
                        .user(usersRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException(userId)))
                        .build());
    }
}
