package com.storix.api.domain.notification.controller.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminNotificationWrapperDto(
        List<AdminNotificationSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static AdminNotificationWrapperDto from(Page<AdminNotificationSummaryResponse> page) {
        return new AdminNotificationWrapperDto(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
