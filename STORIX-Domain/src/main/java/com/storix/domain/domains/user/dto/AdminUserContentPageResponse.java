package com.storix.domain.domains.user.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminUserContentPageResponse(
        List<AdminUserContentItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static AdminUserContentPageResponse from(Page<AdminUserContentItemResponse> page) {
        return new AdminUserContentPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
