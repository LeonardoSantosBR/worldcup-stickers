package com.leonardo.worldcup_stickers.entities;

import com.leonardo.worldcup_stickers.enums.TradeDirection;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trade_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_request_id", nullable = false)
    private TradeRequest tradeRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeDirection direction;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}