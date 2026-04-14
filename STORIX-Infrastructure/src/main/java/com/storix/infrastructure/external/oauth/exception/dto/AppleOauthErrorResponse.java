package com.storix.infrastructure.external.oauth.exception.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.storix.domain.domains.user.exception.oauth.UnHandleException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AppleOauthErrorResponse(
        String error,
        String errorDescription
) {
    public static AppleOauthErrorResponse from(feign.Response response) {
        try (var is = response.body().asInputStream()) {
            byte[] raw = is.readAllBytes();
            String rawBody = new String(raw, StandardCharsets.UTF_8);
            log.error("[Apple] 원본 에러 응답 본문: {}", rawBody);
            return new ObjectMapper().readValue(raw, AppleOauthErrorResponse.class);
        } catch (IOException e) {
            log.error("[Apple] 에러 응답 JSON 파싱 실패: type={}, message={}",
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw UnHandleException.EXCEPTION;
        }
    }
}
