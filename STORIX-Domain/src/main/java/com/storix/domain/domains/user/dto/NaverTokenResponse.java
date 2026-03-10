package com.storix.domain.domains.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverTokenResponse(
        @JsonProperty("access_token") String accessToken
        // @JsonProperty("id_token") String idToken
) {
}
