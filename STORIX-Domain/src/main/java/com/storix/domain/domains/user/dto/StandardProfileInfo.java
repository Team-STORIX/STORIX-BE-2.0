package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.Role;

public record StandardProfileInfo(
        Long userId,
        String profileImageUrl,
        String nickName,
        Role role
) {
    public StandardProfileInfo withBaseUrl(String baseUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return this;
        }

        return new StandardProfileInfo(
                userId,
                baseUrl + "/" + profileImageUrl,
                nickName,
                role
        );
    }
}
