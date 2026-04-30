package com.storix.api.domain.user.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReaderLoginResponse(
        String accessToken,
        String refreshToken
) {
    public static ReaderLoginResponse webLogin(String accessToken) {
        return new ReaderLoginResponse(accessToken, null);
    }

    public static ReaderLoginResponse nativeLogin(String accessToken, String refreshToken) {
        return new ReaderLoginResponse(accessToken, refreshToken);
    }
}
