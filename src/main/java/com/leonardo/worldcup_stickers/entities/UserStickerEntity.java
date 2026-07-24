package com.leonardo.worldcup_stickers.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_stickers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "sticker_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStickerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sticker_id", nullable = false)
    private StickerEntity sticker;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}