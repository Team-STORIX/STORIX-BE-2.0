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
        LocalDateTime timestamp,
        List<FieldErrorResponse> fieldErrors
) {

    public ErrorResponse(ErrorCode errorCode) {
        this(false, errorCode.getCode(), errorCode.getMessage(), LocalDateTime.now(), null);
    }

    public ErrorResponse(ErrorCode errorCode, List<FieldErrorResponse> fieldErrors) {
        this(false, errorCode.getCode(), errorCode.getMessage(), LocalDateTime.now(), fieldErrors);
    }

}