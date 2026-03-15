package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class OidcJwksRefreshRequiredException extends STORIXCodeException {

    public OidcJwksRefreshRequiredException() {
        super(ErrorCode.OIDC_OLD_PUBLIC_KEY_ERROR);
    }
}