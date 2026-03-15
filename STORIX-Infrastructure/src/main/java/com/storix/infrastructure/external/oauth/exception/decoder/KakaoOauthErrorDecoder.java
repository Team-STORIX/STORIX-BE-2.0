package com.storix.infrastructure.external.oauth.exception.decoder;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.infrastructure.external.oauth.exception.dto.KakaoOauthErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;

public class KakaoOauthErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        KakaoOauthErrorResponse body = KakaoOauthErrorResponse.from(response);

        try {
            ErrorCode kakaoErrorCode = ErrorCode.valueOf(body.errorCode());
            return new STORIXCodeException(kakaoErrorCode);
        } catch (IllegalArgumentException e) {
            return new STORIXCodeException(ErrorCode.KOE_INVALID_REQUEST);
        }
    }
}
