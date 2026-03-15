package com.storix.storix_api.global.apiPayload.exception.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NaverOauthErrorResponse(
        String error,
        String errorMessage
) {
    public static NaverOauthErrorResponse from(feign.Response response) {
        try (var is = response.body().asInputStream()) {
            return new ObjectMapper().readValue(is, NaverOauthErrorResponse.class);
        } catch (IOException e) {
            throw UnHandleException.EXCEPTION;
        }
    }
}
