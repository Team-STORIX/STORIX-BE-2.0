package com.storix.domain.domains.user.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminUserReportPageResponse(
        List<AdminUserReportItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static AdminUserReportPageResponse from(Page<AdminUserReportItemResponse> page) {
        return new AdminUserReportPageResponse(
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
