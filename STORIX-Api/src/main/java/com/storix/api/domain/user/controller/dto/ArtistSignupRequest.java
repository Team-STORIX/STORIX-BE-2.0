package com.storix.api.domain.user.controller.dto;

public record ArtistSignupRequest(
        String nickName,
        String loginId,
        String password
) {
}
