package com.storix.domain.domains.user.exception.me;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ProfileNicknameBannedWordException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ProfileNicknameBannedWordException();

    private ProfileNicknameBannedWordException() { super(ErrorCode.PROFILE_NICKNAME_BANNED_WORD); }
}
