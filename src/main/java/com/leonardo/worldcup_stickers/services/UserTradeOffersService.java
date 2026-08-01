package com.leonardo.worldcup_stickers.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leonardo.worldcup_stickers.dto.MakeOfferDto;
import com.leonardo.worldcup_stickers.dto.PageResponseDto;
import com.leonardo.worldcup_stickers.dto.TradeOfferDetailDto;
import com.leonardo.worldcup_stickers.dto.TradeOfferDto;
import com.leonardo.worldcup_stickers.entities.StickerEntity;
import com.leonardo.worldcup_stickers.entities.UserEntity;
import com.leonardo.worldcup_stickers.entities.UserStickerEntity;
import com.leonardo.worldcup_stickers.entities.UserTradeInventoryEntity;
import com.leonardo.worldcup_stickers.entities.UserTradeOffersEntity;
import com.leonardo.worldcup_stickers.entities.UserTradeOffersLogsEntity;
import com.leonardo.worldcup_stickers.enums.TradeStatusEnum;
import com.leonardo.worldcup_stickers.exceptions.InvalidTradeOfferException;
import com.leonardo.worldcup_stickers.exceptions.StickersNotAvailableForTradeException;
import com.leonardo.worldcup_stickers.exceptions.StickersNotOwnedException;
import com.leonardo.worldcup_stickers.exceptions.TradeOfferNotFoundException;
import com.leonardo.worldcup_stickers.exceptions.UserNotFoundException;
import com.leonardo.worldcup_stickers.repositories.StickersRepository;
import com.leonardo.worldcup_stickers.repositories.UserStickersRepository;
import com.leonardo.worldcup_stickers.repositories.UserTradeInventoriesRepository;
import com.leonardo.worldcup_stickers.repositories.UserTradeOffersLogsRepository;
import com.leonardo.worldcup_stickers.repositories.UserTradeOffersRepository;
import com.leonardo.worldcup_stickers.repositories.UsersRepository;

@Service
public class UserTradeOffersService {
    private static final int MAX_LIMIT = 100;

    private final UserTradeOffersRepository userTradeOffersRepository;
    private final UserTradeOffersLogsRepository userTradeOffersLogsRepository;
    private final UserTradeInventoriesRepository userTradeInventoriesRepository;
    private final UserStickersRepository userStickersRepository;
    private final UsersRepository usersRepository;
    private final StickersRepository stickersRepository;

    public UserTradeOffersService(
            UserTradeOffersRepository userTradeOffersRepository,
            UserTradeOffersLogsRepository userTradeOffersLogsRepository,
            UserTradeInventoriesRepository userTradeInventoriesRepository,
            UserStickersRepository userStickersRepository,
            UsersRepository usersRepository,
            StickersRepository stickersRepository) {
        this.userTradeOffersRepository = userTradeOffersRepository;
        this.userTradeOffersLogsRepository = userTradeOffersLogsRepository;
        this.userTradeInventoriesRepository = userTradeInventoriesRepository;
        this.userStickersRepository = userStickersRepository;
        this.usersRepository = usersRepository;
        this.stickersRepository = stickersRepository;
    }

    @Transactional(readOnly = true)
    public PageResponseDto<TradeOfferDetailDto> findReceivedOffers(Long receiverId, int page, int limit, TradeStatusEnum status) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.min(Math.max(limit, 1), MAX_LIMIT),
                Sort.by("createdAt").descending());

        Page<UserTradeOffersEntity> result = status == null
                ? userTradeOffersRepository.findByReceiverId(receiverId, pageable)
                : userTradeOffersRepository.findByReceiverIdAndStatus(receiverId, status, pageable);

        Map<Long, String> stickerNames = loadStickerNames(result.getContent());
        return PageResponseDto.from(result, offer -> TradeOfferDetailDto.fromEntity(offer, stickerNames));
    }

    private Map<Long, String> loadStickerNames(List<UserTradeOffersEntity> offers) {
        Set<Long> stickerIds = offers.stream()
                .flatMap(offer -> Stream.concat(
                        offer.getRequestedStickerIds().stream(),
                        offer.getOfferedStickerIds().stream()))
                .collect(Collectors.toSet());

        if (stickerIds.isEmpty()) {
            return Map.of();
        }

        return stickersRepository.findAllById(stickerIds).stream()
                .collect(Collectors.toMap(StickerEntity::getId, StickerEntity::getPlayerName));
    }

    @Transactional
    public TradeOfferDto makeOffer(Long proposerId, MakeOfferDto body) {
        if (proposerId.equals(body.receiverId())) {
            throw new InvalidTradeOfferException("Cannot make a trade offer to yourself");
        }

        Set<Long> requested = new LinkedHashSet<>(body.requestedStickerIds());
        Set<Long> offered = new LinkedHashSet<>(body.offeredStickerIds());

        Set<Long> overlap = new LinkedHashSet<>(requested);
        overlap.retainAll(offered);
        if (!overlap.isEmpty()) {
            throw new InvalidTradeOfferException(
                    "The same sticker cannot be requested and offered: " + overlap);
        }

        UserEntity proposer = usersRepository.findById(proposerId)
                .orElseThrow(() -> new UserNotFoundException(proposerId));
        UserEntity receiver = usersRepository.findById(body.receiverId())
                .orElseThrow(() -> new UserNotFoundException(body.receiverId()));

        // the proposer must own everything they are offering
        Set<Long> ownedByProposer = new HashSet<>(userStickersRepository.findStickerIdsByUserId(proposerId));
        Set<Long> notOwned = new LinkedHashSet<>(offered);
        notOwned.removeAll(ownedByProposer);
        if (!notOwned.isEmpty()) {
            throw new StickersNotOwnedException(notOwned);
        }

        Set<Long> availableFromReceiver = userTradeInventoriesRepository.findByUserId(receiver.getId())
                .map(UserTradeInventoryEntity::getAvailableStickerIds)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        Set<Long> notAvailable = new LinkedHashSet<>(requested);
        notAvailable.removeAll(availableFromReceiver);
        if (!notAvailable.isEmpty()) {
            throw new StickersNotAvailableForTradeException(receiver.getId(), notAvailable);
        }

        UserTradeOffersEntity offer = UserTradeOffersEntity.builder()
                .proposer(proposer)
                .receiver(receiver)
                .requestedStickerIds(new ArrayList<>(requested))
                .offeredStickerIds(new ArrayList<>(offered))
                .status(TradeStatusEnum.PENDING)
                .message(body.message())
                .build();

        UserTradeOffersEntity saved = userTradeOffersRepository.save(offer);
        log(saved, TradeStatusEnum.PENDING, proposer, "Offer created");

        return TradeOfferDto.fromEntity(saved);
    }
    
    @Transactional
    public TradeOfferDto acceptOffer(Long receiverId, Long offerId, String note) {
        UserTradeOffersEntity offer = loadPendingOfferForReceiver(receiverId, offerId);

        UserEntity proposer = offer.getProposer();
        UserEntity receiver = offer.getReceiver();

        // re-check ownership on both sides — state may have changed since the offer
        assertOwns(proposer, offer.getOfferedStickerIds());
        assertOwns(receiver, offer.getRequestedStickerIds());

        transferStickers(receiver, proposer, offer.getRequestedStickerIds());
        transferStickers(proposer, receiver, offer.getOfferedStickerIds());

        syncTradeInventory(proposer.getId());
        syncTradeInventory(receiver.getId());

        offer.setStatus(TradeStatusEnum.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        UserTradeOffersEntity saved = userTradeOffersRepository.save(offer);
        log(saved, TradeStatusEnum.ACCEPTED, receiver, note);

        invalidateConflictingOffers(saved, receiver);

        return TradeOfferDto.fromEntity(saved);
    }

    @Transactional
    public TradeOfferDto rejectOffer(Long receiverId, Long offerId, String note) {
        UserTradeOffersEntity offer = loadPendingOfferForReceiver(receiverId, offerId);

        offer.setStatus(TradeStatusEnum.REJECTED);
        offer.setRespondedAt(LocalDateTime.now());
        UserTradeOffersEntity saved = userTradeOffersRepository.save(offer);
        log(saved, TradeStatusEnum.REJECTED, offer.getReceiver(), note);

        return TradeOfferDto.fromEntity(saved);
    }

    private UserTradeOffersEntity loadPendingOfferForReceiver(Long receiverId, Long offerId) {
        UserTradeOffersEntity offer = userTradeOffersRepository.findByIdAndReceiverId(offerId, receiverId)
                .orElseThrow(() -> new TradeOfferNotFoundException(offerId));

        if (offer.getStatus() != TradeStatusEnum.PENDING) {
            throw new InvalidTradeOfferException(
                    "Trade offer " + offerId + " is already " + offer.getStatus());
        }
        return offer;
    }

    private void assertOwns(UserEntity user, List<Long> stickerIds) {
        Set<Long> owned = new HashSet<>(userStickersRepository.findStickerIdsByUserId(user.getId()));
        Set<Long> missing = new LinkedHashSet<>(stickerIds);
        missing.removeAll(owned);
        if (!missing.isEmpty()) {
            throw new StickersNotOwnedException(missing);
        }
    }

    private void transferStickers(UserEntity from, UserEntity to, List<Long> stickerIds) {
        for (Long stickerId : stickerIds) {
            UserStickerEntity source = userStickersRepository
                    .findByUserIdAndStickerId(from.getId(), stickerId)
                    .orElseThrow(() -> new StickersNotOwnedException(List.of(stickerId)));

            StickerEntity sticker = source.getSticker();

            if (source.getQuantity() <= 1) {
                userStickersRepository.delete(source);
            } else {
                source.setQuantity(source.getQuantity() - 1);
                userStickersRepository.save(source);
            }

            UserStickerEntity target = userStickersRepository
                    .findByUserIdAndStickerId(to.getId(), stickerId)
                    .orElseGet(() -> UserStickerEntity.builder()
                            .user(to)
                            .sticker(sticker)
                            .quantity(0)
                            .build());
            target.setQuantity(target.getQuantity() + 1);
            userStickersRepository.save(target);
        }
    }

    private void syncTradeInventory(Long userId) {
        userTradeInventoriesRepository.findByUserId(userId).ifPresent(inventory -> {
            Set<Long> owned = new HashSet<>(userStickersRepository.findStickerIdsByUserId(userId));
            List<Long> stillOwned = inventory.getAvailableStickerIds().stream()
                    .filter(owned::contains)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (stillOwned.size() != inventory.getAvailableStickerIds().size()) {
                inventory.setAvailableStickerIds(stillOwned);
                userTradeInventoriesRepository.save(inventory);
            }
        });
    }


    private void invalidateConflictingOffers(UserTradeOffersEntity accepted, UserEntity actor) {
        Set<Long> tradedUserIds = Set.of(accepted.getProposer().getId(), accepted.getReceiver().getId());

        Map<Long, Set<Long>> ownedByUser = new HashMap<>();
        for (Long userId : tradedUserIds) {
            ownedByUser.put(userId, new HashSet<>(userStickersRepository.findStickerIdsByUserId(userId)));
        }

        List<UserTradeOffersEntity> pending =
                userTradeOffersRepository.findByStatusAndUsersInvolved(TradeStatusEnum.PENDING, tradedUserIds);

        for (UserTradeOffersEntity other : pending) {
            if (other.getId().equals(accepted.getId())) {
                continue;
            }

            Set<Long> proposerOwns = ownedByUser.get(other.getProposer().getId());
            Set<Long> receiverOwns = ownedByUser.get(other.getReceiver().getId());

            boolean broken =
                    (proposerOwns != null && !proposerOwns.containsAll(other.getOfferedStickerIds()))
                    || (receiverOwns != null && !receiverOwns.containsAll(other.getRequestedStickerIds()));

            if (broken) {
                other.setStatus(TradeStatusEnum.REJECTED);
                other.setRespondedAt(LocalDateTime.now());
                UserTradeOffersEntity savedOther = userTradeOffersRepository.save(other);
                log(savedOther, TradeStatusEnum.REJECTED, actor,
                        "Automatically rejected: stickers no longer available");
            }
        }
    }

    private void log(UserTradeOffersEntity offer, TradeStatusEnum status, UserEntity changedBy, String note) {
        userTradeOffersLogsRepository.save(UserTradeOffersLogsEntity.builder()
                .tradeOffer(offer)
                .status(status)
                .changedBy(changedBy)
                .note(note)
                .build());
    }
}
