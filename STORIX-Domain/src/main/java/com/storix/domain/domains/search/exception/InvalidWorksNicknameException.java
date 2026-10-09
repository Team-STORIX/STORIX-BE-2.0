package com.storix.domain.domains.search.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidWorksNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidWorksNicknameException();
    private InvalidWorksNicknameException() { super(ErrorCode.INVALID_WORKS_NICKNAME); }
}
