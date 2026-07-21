package com.storix.domain.domains.appversion.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidAppVersionException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidAppVersionException();

    private InvalidAppVersionException() { super(ErrorCode.INVALID_APP_VERSION_FORMAT); }
}
