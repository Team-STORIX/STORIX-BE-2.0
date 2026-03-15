package com.storix.storix_api.domains.user.controller.dto;

public record ArtistSignupRequest(
        String nickName,
        String loginId,
        String password
) {
}
