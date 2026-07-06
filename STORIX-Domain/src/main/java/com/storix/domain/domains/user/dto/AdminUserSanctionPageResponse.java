package com.storix.domain.domains.user.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminUserSanctionPageResponse(
        List<AdminUserSanctionDetailResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static AdminUserSanctionPageResponse from(Page<?> page, List<AdminUserSanctionDetailResponse> content) {
        return new AdminUserSanctionPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
