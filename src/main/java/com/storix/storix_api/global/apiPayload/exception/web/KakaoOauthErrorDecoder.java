package com.storix.storix_api.global.apiPayload.exception.web;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;
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
