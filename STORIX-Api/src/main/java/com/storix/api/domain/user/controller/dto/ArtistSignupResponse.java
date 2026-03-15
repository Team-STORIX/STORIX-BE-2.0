package com.storix.api.domain.user.controller.dto;

public record ArtistSignupResponse(
        Long userId,
        String loginId,
        String nickname
) {
}
