package com.storix.domain.domains.bannedword.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateBannedWordException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateBannedWordException();

    private DuplicateBannedWordException() {
        super(ErrorCode.DUPLICATE_BANNED_WORD);
    }
}
