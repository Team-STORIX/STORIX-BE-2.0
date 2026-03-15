package com.storix.storix_api.global.apiPayload.exception.web;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class KakaoInfoErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response){
        if (response.status() >= 400) {
            switch (response.status()) {
                case 401 -> throw FeignClientForbiddenException.EXCEPTION;
                case 403 -> throw FeignClientUnauthorizedException.EXCEPTION;
                case 419 -> throw FeignClientExpiredTokenException.EXCEPTION;
                default -> throw FeignClientBadRequestException.EXCEPTION;
            }
        }

        return FeignException.errorStatus(methodKey, response);
    }
}
