package com.storix.infrastructure.external.oauth.exception.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.storix.domain.domains.user.exception.oauth.UnHandleException;

import java.io.IOException;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AppleOauthErrorResponse(
        String error,
        String errorDescription
) {
    public static AppleOauthErrorResponse from(feign.Response response) {
        try (var is = response.body().asInputStream()) {
            return new ObjectMapper().readValue(is, AppleOauthErrorResponse.class);
        } catch (IOException e) {
            throw UnHandleException.EXCEPTION;
        }
    }
}
