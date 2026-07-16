package com.storix.common.payload;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseWrapperDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PageResponseWrapperDTO<T> from(Page<T> page) {
        return new PageResponseWrapperDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
