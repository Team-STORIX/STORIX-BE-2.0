package com.storix.domain.domains.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record XTokenResponse(
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("scope") String scope,
        @JsonProperty("refresh_token") String refreshToken
) {
}
