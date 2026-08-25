package com.storix.infrastructure.external.portone.exception.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.domain.domains.adultverification.exception.IdentityVerificationProviderException;
import com.storix.domain.domains.adultverification.exception.IncompleteIdentityVerificationException;
import com.storix.infrastructure.external.portone.exception.dto.PortOneErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PortOneErrorDecoder implements ErrorDecoder {

    private static final String IDENTITY_VERIFICATION_NOT_FOUND = "IDENTITY_VERIFICATION_NOT_FOUND";

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        String type = PortOneErrorResponse.readType(response, objectMapper);

        // 앱이 인증창을 띄우지 않았거나 인증을 끝내지 않은 경우
        if (IDENTITY_VERIFICATION_NOT_FOUND.equals(type)) {
            return IncompleteIdentityVerificationException.EXCEPTION;
        }

        log.error("포트원 본인인증 조회 실패 status={} type={}", response.status(), type);
        return IdentityVerificationProviderException.EXCEPTION;
    }
}
