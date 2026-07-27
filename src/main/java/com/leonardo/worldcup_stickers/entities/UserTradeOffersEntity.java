package com.leonardo.worldcup_stickers.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.leonardo.worldcup_stickers.enums.TradeStatusEnum;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_trade_offers", indexes = {
    @Index(name = "idx_trade_offers_receiver_status", columnList = "receiver_id, status"),
    @Index(name = "idx_trade_offers_proposer_status", columnList = "proposer_id, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTradeOffersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", nullable = false)
    private UserEntity proposer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "requested_sticker_ids", nullable = false, columnDefinition = "bigint[]")
    @Builder.Default
    private List<Long> requestedStickerIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "offered_sticker_ids", nullable = false, columnDefinition = "bigint[]")
    @Builder.Default
    private List<Long> offeredStickerIds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TradeStatusEnum status = TradeStatusEnum.PENDING;

    @Column(length = 255)
    private String message;

    private LocalDateTime respondedAt;

    @OneToMany(mappedBy = "tradeOffer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserTradeOffersLogsEntity> logs = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
