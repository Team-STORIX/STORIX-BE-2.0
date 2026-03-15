package com.storix.storix_api.domains.user.controller.dto;

public record ArtistSignupResponse(
        Long userId,
        String loginId,
        String nickname
) {
}
