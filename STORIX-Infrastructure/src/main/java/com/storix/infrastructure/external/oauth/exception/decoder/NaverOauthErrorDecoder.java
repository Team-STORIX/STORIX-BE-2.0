package com.storix.infrastructure.external.oauth.exception.decoder;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.infrastructure.external.oauth.exception.dto.NaverOauthErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaverOauthErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        NaverOauthErrorResponse body = NaverOauthErrorResponse.from(response);
        ErrorCode errorCode = resolveErrorCode(body.error());
        return new STORIXCodeException(errorCode);
    }

    private ErrorCode resolveErrorCode(String rawErrorCode) {

        if (rawErrorCode == null || rawErrorCode.isBlank()) {
            return ErrorCode.NOE_INVALID_REQUEST;
        }

        // ex) "024" → "NOE024"
        // ex) "invalid_request" → "NOE_INVALID_REQUEST"
        String enumKey = "NOE" + rawErrorCode
                .toUpperCase()
                .replace("-", "_");

        try {
            return ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            return ErrorCode.NOE_INVALID_REQUEST;
        }
    }
}