package com.storix.common.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.common.code.SuccessCode;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomResponse<T> (
        Boolean isSuccess,
        String code,
        String message,
        T result,
        LocalDateTime timestamp
) {

    public static <T> CustomResponse<T> onSuccess(SuccessCode successCode) {
        return CustomResponse.<T>builder()
                .isSuccess(true)
                .code(successCode.getCode())
                .message(successCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> CustomResponse<T> onSuccess(SuccessCode successCode, T result) {
        return CustomResponse.<T>builder()
                .isSuccess(true)
                .code(successCode.getCode())
                .message(successCode.getMessage())
                .result(result)
                .timestamp(LocalDateTime.now())
                .build();
    }
}