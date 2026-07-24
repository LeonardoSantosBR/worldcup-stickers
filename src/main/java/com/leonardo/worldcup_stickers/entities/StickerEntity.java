package com.leonardo.worldcup_stickers.entities;

import com.leonardo.worldcup_stickers.enums.PositionEnum;
import com.leonardo.worldcup_stickers.enums.RarityEnum;
import com.leonardo.worldcup_stickers.enums.StickerGroupEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "stickers")
@SQLDelete(sql = "UPDATE stickers SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StickerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer number;

    @Column(nullable = false)
    private String playerName;

    @Column(nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "sticker_group")
    private StickerGroupEnum group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionEnum position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RarityEnum rarity = RarityEnum.COMMON;

    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}