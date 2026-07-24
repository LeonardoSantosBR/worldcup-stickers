package com.leonardo.worldcup_stickers.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PageResponseDto<T>(
    List<T> items,
    int page,
    int limit,
    long totalItems,
    int totalPages,
    boolean hasNext) {

    public static <E, T> PageResponseDto<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponseDto<>(
            page.getContent().stream().map(mapper).toList(),
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext());
    }
}
