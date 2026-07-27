package com.leonardo.worldcup_stickers.entities;

import java.time.LocalDateTime;

import com.leonardo.worldcup_stickers.enums.TradeStatusEnum;

import jakarta.persistence.*;
import lombok.*;

/**
 * Historico imutavel de cada mudanca de status de uma oferta de troca.
 *
 * Uma linha e gravada em toda transicao: PENDING (ofertado) na criacao,
 * ACCEPTED / REJECTED quando o receiver responde, CANCELLED quando o
 * proposer desiste.
 */
@Entity
@Table(name = "user_trade_offers_logs", indexes = {
    @Index(name = "idx_trade_offer_logs_offer", columnList = "trade_offer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTradeOffersLogsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_offer_id", nullable = false)
    private UserTradeOffersEntity tradeOffer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TradeStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private UserEntity changedBy;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
