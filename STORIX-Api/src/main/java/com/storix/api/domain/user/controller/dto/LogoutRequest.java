package com.storix.api.domain.user.controller.dto;

public record LogoutRequest(
        String installationId   // 기기 식별자
) {
}
