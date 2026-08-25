package com.storix.infrastructure.external.portone.exception.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;

import java.io.IOException;
import java.io.InputStream;

public record PortOneErrorResponse(
        String type,
        String message
) {

    // 본문이 없거나 형식이 달라도 디코더는 예외를 만들어야 한다
    public static String readType(Response response, ObjectMapper objectMapper) {
        if (response.body() == null) {
            return null;
        }
        try (InputStream body = response.body().asInputStream()) {
            return objectMapper.readValue(body, PortOneErrorResponse.class).type();
        } catch (IOException e) {
            return null;
        }
    }
}
