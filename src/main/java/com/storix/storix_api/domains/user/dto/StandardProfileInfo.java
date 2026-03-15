package com.storix.storix_api.domains.user.dto;

public record StandardProfileInfo(
        Long userId,
        String profileImageUrl,
        String nickName
) {
    public StandardProfileInfo withBaseUrl(String baseUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return this;
        }

        return new StandardProfileInfo(
                userId,
                baseUrl + "/" + profileImageUrl,
                nickName
        );
    }
}