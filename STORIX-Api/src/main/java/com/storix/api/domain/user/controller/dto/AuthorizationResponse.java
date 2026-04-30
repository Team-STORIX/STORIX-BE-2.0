package com.storix.api.domain.user.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorizationResponse(
        String accessToken,
        String refreshToken
) {
    public static AuthorizationResponse webRefresh(String accessToken) {
        return new AuthorizationResponse(accessToken, null);
    }

    public static AuthorizationResponse nativeRefresh(String accessToken, String refreshToken) {
        return new AuthorizationResponse(accessToken, refreshToken);
    }
}
