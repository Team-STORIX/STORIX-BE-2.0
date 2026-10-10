package com.storix.common.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.common.code.ErrorCode;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse (
        Boolean isSuccess,
        String code,
        String message,
        String detail,
        LocalDateTime timestamp,
        List<FieldErrorResponse> fieldErrors
) {

    public ErrorResponse(ErrorCode errorCode) {
        this(false, errorCode.getCode(), errorCode.getMessage(), null, LocalDateTime.now(), null);
    }

    public ErrorResponse(ErrorCode errorCode, List<FieldErrorResponse> fieldErrors) {
        this(false, errorCode.getCode(), errorCode.getMessage(), null, LocalDateTime.now(), fieldErrors);
    }

    public static ErrorResponse withDetail(ErrorCode errorCode, String detail) {
        return new ErrorResponse(false, errorCode.getCode(), errorCode.getMessage(), detail, LocalDateTime.now(), null);
    }

}