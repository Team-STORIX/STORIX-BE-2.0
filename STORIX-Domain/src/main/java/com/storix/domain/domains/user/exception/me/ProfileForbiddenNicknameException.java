package com.storix.domain.domains.user.exception.me;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ProfileForbiddenNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ProfileForbiddenNicknameException();

    private ProfileForbiddenNicknameException() { super(ErrorCode.PROFILE_FORBIDDEN_NICKNAME); }
}