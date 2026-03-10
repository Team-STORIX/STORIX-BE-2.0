package com.storix.domain.domains.user.exception.me;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateUserException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateUserException();

    private DuplicateUserException() { super(ErrorCode.DUPLICATE_USER_SIGN); }
}