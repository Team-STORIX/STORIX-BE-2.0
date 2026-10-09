package com.storix.domain.domains.search.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class WorksNicknameNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new WorksNicknameNotFoundException();
    private WorksNicknameNotFoundException() { super(ErrorCode.WORKS_NICKNAME_NOT_FOUND); }
}
