package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ApplePrivateKeyException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ApplePrivateKeyException();

    private ApplePrivateKeyException() { super(ErrorCode.AOE_PRIVATE_KEY_ERROR); }
}
