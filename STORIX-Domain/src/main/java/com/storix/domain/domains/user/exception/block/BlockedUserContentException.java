package com.storix.domain.domains.user.exception.block;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BlockedUserContentException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BlockedUserContentException();

    private BlockedUserContentException() {
        super(ErrorCode.BLOCKED_USER_CONTENT);
    }
}
