package com.storix.domain.domains.user.exception.me;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ProfileInvalidNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ProfileInvalidNicknameException();

    private ProfileInvalidNicknameException() { super(ErrorCode.PROFILE_FORBIDDEN_NICKNAME); }
}
