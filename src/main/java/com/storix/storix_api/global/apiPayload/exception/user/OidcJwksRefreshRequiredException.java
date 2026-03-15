package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class OidcJwksRefreshRequiredException extends STORIXCodeException {

    public OidcJwksRefreshRequiredException() {
        super(ErrorCode.OIDC_OLD_PUBLIC_KEY_ERROR);
    }
}