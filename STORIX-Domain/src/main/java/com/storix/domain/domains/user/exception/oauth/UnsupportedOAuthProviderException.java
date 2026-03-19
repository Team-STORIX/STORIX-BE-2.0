package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnsupportedOAuthProviderException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnsupportedOAuthProviderException();

    private UnsupportedOAuthProviderException() { super(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER); }
}
