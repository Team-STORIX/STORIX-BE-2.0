package com.storix.domain.domains.user.dto;

public record ValidAuthDTO(
        boolean isRegistered,
        String idToken
) {
}
