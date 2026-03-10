package com.storix.api.global;

import com.storix.api.domain.user.helper.CookieHelper;
import com.storix.common.code.ErrorCode;
import com.storix.common.payload.ErrorResponse;
import com.storix.common.payload.FieldErrorResponse;
import com.storix.common.exception.STORIXCodeException;
import com.storix.common.exception.STORIXCookieException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CookieHelper cookieHelper;

    @ExceptionHandler(STORIXCodeException.class)
    public ResponseEntity<ErrorResponse> STORIXCodeExceptionHandler (STORIXCodeException ex) {

        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = new ErrorResponse(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(STORIXCookieException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenNotExist(STORIXCookieException ex) {

        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse body = new ErrorResponse(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .headers(cookieHelper.deleteCookie())
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException (MethodArgumentNotValidException e) {

        List<FieldErrorResponse> fieldErrors =
                e.getBindingResult().getFieldErrors().stream()
                        .map(fe -> new FieldErrorResponse(
                                fe.getField(),
                                fe.getRejectedValue(),
                                fe.getCode(),
                                fe.getDefaultMessage()
                        ))
                        .toList();

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        ErrorResponse response = new ErrorResponse(errorCode, fieldErrors);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {

        List<FieldErrorResponse> fieldErrors = e.getConstraintViolations().stream()
                .map(v -> FieldErrorResponse.builder()
                        .field(v.getPropertyPath().toString())
                        .rejectedValue(v.getInvalidValue())
                        .reason(v.getMessage())
                        .build())
                .toList();

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        ErrorResponse response = new ErrorResponse(errorCode, fieldErrors);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParamException (MissingServletRequestParameterException e) {

        FieldErrorResponse fer = FieldErrorResponse.builder()
                .field(e.getParameterName())
                .rejectedValue(null)
                .reason("필수 파라미터가 누락되었습니다.")
                .build();

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        ErrorResponse response = new ErrorResponse(errorCode, List.of(fer));

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {

        String field = "Database";
        Object rejectedValue = null;
        String reason = "데이터 무결성 제약조건 위반입니다.";

        Throwable root = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);
        String rootMsg = root.getMessage() != null ? root.getMessage() : "";

        Matcher m1 = Pattern
                .compile("Column '([^']+)' cannot be null")
                .matcher(rootMsg);
        if (m1.find()) {
            field = m1.group(1);
            reason = "null이 될 수 없는 필드입니다.";
        }

        Matcher m2 = Pattern
                .compile("Duplicate entry '([^']*)' for key '([^']+)'")
                .matcher(rootMsg);
        if (m2.find()) {
            rejectedValue = m2.group(1);
            field = m2.group(2);
            reason = "중복된 값입니다.";
        }

        if (rootMsg.contains("foreign key constraint fails")) {
            field = "ForeignKey";
            reason = "참조 무결성 제약조건 위반입니다.";
        }

        FieldErrorResponse fer = FieldErrorResponse.builder()
                .field(field)
                .rejectedValue(rejectedValue)
                .reason(reason)
                .build();

        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION_REQUEST;
        ErrorResponse response = new ErrorResponse(errorCode, java.util.List.of(fer));

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageConversionException e) {

        log.warn("Format exception", e);

        ErrorCode errorCode = ErrorCode.INVALID_JSON_REQUEST;
        ErrorResponse response = new ErrorResponse(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception e) {

        log.error("Unhandled exception", e);

        ErrorCode errorCode = ErrorCode.UNHANDLED_ERROR;
        ErrorResponse response = new ErrorResponse(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

}
