package com.storix.domain.domains.search.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateWorksNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateWorksNicknameException();
    private DuplicateWorksNicknameException() { super(ErrorCode.DUPLICATE_WORKS_NICKNAME); }
}
