package com.leonardo.worldcup_stickers.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.leonardo.worldcup_stickers.entities.UserTradeOffersEntity;
import com.leonardo.worldcup_stickers.enums.TradeStatusEnum;

public interface UserTradeOffersRepository extends JpaRepository<UserTradeOffersEntity, Long> {

    /** Ofertas recebidas — a caixa de entrada do usuario. */
    Page<UserTradeOffersEntity> findByReceiverIdAndStatus(Long receiverId, TradeStatusEnum status, Pageable pageable);

    /** Ofertas enviadas pelo usuario. */
    Page<UserTradeOffersEntity> findByProposerIdAndStatus(Long proposerId, TradeStatusEnum status, Pageable pageable);

    Optional<UserTradeOffersEntity> findByIdAndReceiverId(Long id, Long receiverId);

    Optional<UserTradeOffersEntity> findByIdAndProposerId(Long id, Long proposerId);

    /** Todas as ofertas pendentes envolvendo um usuario — usado para invalidar em lote apos uma troca aceita. */
    List<UserTradeOffersEntity> findByStatusAndProposerIdOrStatusAndReceiverId(
            TradeStatusEnum proposerStatus, Long proposerId,
            TradeStatusEnum receiverStatus, Long receiverId);

    /** Ofertas em um dado status que envolvem qualquer um dos usuarios informados. */
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
