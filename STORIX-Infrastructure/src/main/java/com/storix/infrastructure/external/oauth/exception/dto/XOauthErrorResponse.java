package com.storix.infrastructure.external.oauth.exception.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.storix.domain.domains.user.exception.oauth.UnHandleException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record XOauthErrorResponse(
        String error,
        String errorDescription
) {
    public static XOauthErrorResponse from(feign.Response response) {
        try (var is = response.body().asInputStream()) {
            byte[] raw = is.readAllBytes();
            String rawBody = new String(raw, StandardCharsets.UTF_8);
            log.error("[X] 원본 에러 응답 본문: {}", rawBody);
            return new ObjectMapper().readValue(raw, XOauthErrorResponse.class);
        } catch (IOException e) {
            log.error("[X] 에러 응답 JSON 파싱 실패: type={}, message={}",
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw UnHandleException.EXCEPTION;
        }
    }
}
