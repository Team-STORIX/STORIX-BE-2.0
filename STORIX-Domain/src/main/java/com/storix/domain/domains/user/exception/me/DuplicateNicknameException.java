package com.storix.domain.domains.user.exception.me;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateNicknameException();

    private DuplicateNicknameException() { super(ErrorCode.ONBOARDING_DUPLICATE_NICKNAME); }
}