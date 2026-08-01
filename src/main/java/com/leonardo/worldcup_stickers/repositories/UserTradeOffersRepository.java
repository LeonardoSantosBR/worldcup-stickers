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

import com.leonardo.worldcup_stickers.entities.UserTradeOffersEntity;
import com.leonardo.worldcup_stickers.enums.TradeStatusEnum;

public interface UserTradeOffersRepository extends JpaRepository<UserTradeOffersEntity, Long> {

    /**
     * Offers received — the user's inbox, every status.
     *
     * The proposer is fetched eagerly because the listing shows their name; it is a
     * to-one association, so the join does not multiply rows and pagination stays correct.
     */
    @EntityGraph(attributePaths = "proposer")
    Page<UserTradeOffersEntity> findByReceiverId(Long receiverId, Pageable pageable);

    /** Offers received — the user's inbox, filtered by status. */
    @EntityGraph(attributePaths = "proposer")
    Page<UserTradeOffersEntity> findByReceiverIdAndStatus(Long receiverId, TradeStatusEnum status, Pageable pageable);

    /** Offers sent by the user. */
    Page<UserTradeOffersEntity> findByProposerIdAndStatus(Long proposerId, TradeStatusEnum status, Pageable pageable);

    Optional<UserTradeOffersEntity> findByIdAndReceiverId(Long id, Long receiverId);

    Optional<UserTradeOffersEntity> findByIdAndProposerId(Long id, Long proposerId);

    /** Every pending offer involving a user — used to invalidate offers in bulk after a trade is accepted. */
    List<UserTradeOffersEntity> findByStatusAndProposerIdOrStatusAndReceiverId(
            TradeStatusEnum proposerStatus, Long proposerId,
            TradeStatusEnum receiverStatus, Long receiverId);

    /** Offers in a given status involving any of the given users. */
    @Query("""
            SELECT o FROM UserTradeOffersEntity o
            WHERE o.status = :status
              AND (o.proposer.id IN :userIds OR o.receiver.id IN :userIds)
            """)
    List<UserTradeOffersEntity> findByStatusAndUsersInvolved(
            @Param("status") TradeStatusEnum status,
            @Param("userIds") Collection<Long> userIds);

    boolean existsByProposerIdAndReceiverIdAndStatus(Long proposerId, Long receiverId, TradeStatusEnum status);
}
