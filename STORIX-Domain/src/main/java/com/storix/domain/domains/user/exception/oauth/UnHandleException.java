package com.storix.domain.domains.user.exception.oauth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnHandleException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnHandleException();

    private UnHandleException() { super(ErrorCode.BAD_REQUEST); }
}