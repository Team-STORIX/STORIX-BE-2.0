package com.storix.domain.domains.user.exception.me;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ProfileDuplicateNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ProfileDuplicateNicknameException();

    private ProfileDuplicateNicknameException() { super(ErrorCode.PROFILE_DUPLICATE_NICKNAME); }
}