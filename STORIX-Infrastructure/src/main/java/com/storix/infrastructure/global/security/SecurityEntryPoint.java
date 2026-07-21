package com.storix.infrastructure.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.code.ErrorCode;
import com.storix.common.payload.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode code = ErrorCode.TOKEN_NOT_EXIST;

        log.warn(">>> [Http] 인증 실패 code={} status={} message={}",
                code.getCode(), code.getHttpStatus().value(), code.getMessage());

        response.setStatus(code.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(code));
    }
}
