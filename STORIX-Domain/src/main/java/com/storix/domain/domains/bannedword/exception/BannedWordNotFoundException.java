package com.storix.domain.domains.bannedword.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BannedWordNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BannedWordNotFoundException();

    private BannedWordNotFoundException() {
        super(ErrorCode.BANNED_WORD_NOT_FOUND);
    }
}
