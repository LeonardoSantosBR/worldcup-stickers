package com.leonardo.worldcup_stickers.dto;

import com.leonardo.worldcup_stickers.entities.UserEntity;

public record MyProfileDto(
    String email,
    String name,
    double completePercentage) {

    public static MyProfileDto fromEntity(UserEntity user, long ownedStickers, long totalStickers) {
        double percentage = totalStickers == 0
                ? 0
                : Math.round(ownedStickers * 10_000.0 / totalStickers) / 100.0;

        return new MyProfileDto(
            user.getEmail(),
            user.getName(),
            percentage);
    }
}
