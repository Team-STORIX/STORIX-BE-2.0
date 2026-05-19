package com.storix.api.global;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    /**
     * 요청 body 파싱 실패 — Jackson 단계에서 발생.
     * cause 가 InvalidFormatException (enum 값 불일치 / 타입 변환 실패) 이면
     * 어느 필드가 어떤 값으로 거부됐는지, enum 의 경우 허용값 목록까지 응답에 포함.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {

        log.warn("Body parse exception", e);

        if (e.getCause() instanceof InvalidFormatException ife) {
            String field = ife.getPath().stream()
                    .map(InvalidFormatException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));
            if (field.isEmpty()) field = "unknown";

            Class<?> target = ife.getTargetType();
            String reason;
            if (target != null && target.isEnum()) {
                String allowed = Arrays.stream(target.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                reason = "허용되지 않는 값입니다. 허용값: [" + allowed + "]";
            } else {
                String typeName = target != null ? target.getSimpleName() : "올바른 타입";
                reason = typeName + " 형식이 아닙니다.";
            }

            FieldErrorResponse fer = FieldErrorResponse.builder()
                    .field(field)
                    .rejectedValue(ife.getValue())
                    .reason(reason)
                    .build();

            ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
            return ResponseEntity
                    .status(errorCode.getHttpStatus())
                    .body(new ErrorResponse(errorCode, List.of(fer)));
        }

        ErrorCode errorCode = ErrorCode.INVALID_JSON_REQUEST;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode));
    }

    /** 그 외 변환 오류 (response 직렬화 실패 등) — fallback */
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handleConversion(HttpMessageConversionException e) {

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
