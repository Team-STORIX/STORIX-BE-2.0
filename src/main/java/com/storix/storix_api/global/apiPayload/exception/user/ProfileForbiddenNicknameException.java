package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ProfileForbiddenNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ProfileForbiddenNicknameException();

    private ProfileForbiddenNicknameException() { super(ErrorCode.PROFILE_FORBIDDEN_NICKNAME); }
}