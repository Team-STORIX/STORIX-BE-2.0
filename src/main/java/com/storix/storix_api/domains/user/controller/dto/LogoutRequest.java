package com.storix.storix_api.domains.user.controller.dto;

public record LogoutRequest(
        String refreshToken
) {
}
