package com.storix.storix_api.global.apiPayload.exception.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.IOException;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoOauthErrorResponse(
        String error,
        String errorDescription,
        String errorCode
) {
    public static KakaoOauthErrorResponse from(feign.Response response) {
        try (var is = response.body().asInputStream()) {
            return new ObjectMapper().readValue(is, KakaoOauthErrorResponse.class);
        } catch (IOException e) {
            throw UnHandleException.EXCEPTION;
        }
    }
}
