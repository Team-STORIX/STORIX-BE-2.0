package com.storix.domain.domains.user.exception.block;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class SelfBlockException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SelfBlockException();

    private SelfBlockException() {
        super(ErrorCode.SELF_BLOCK_ERROR);
    }
}
