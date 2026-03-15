package com.storix.domain.domains.user.dto;

public record FavoriteArtistInfo(
        Long artistId,
        String profileImageUrl,
        String artistName,
        String profileDescription
) {
    public FavoriteArtistInfo withBaseUrl(String baseUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return this;
        }

        return new FavoriteArtistInfo(
                artistId,
                baseUrl + "/" + profileImageUrl,
                artistName,
                profileDescription
        );
    }
}