package com.storix.domain.domains.user.exception.block;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateUserBlockException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateUserBlockException();

    private DuplicateUserBlockException() {
        super(ErrorCode.DUPLICATE_USER_BLOCK);
    }
}
