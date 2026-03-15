package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ProfileDuplicateNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ProfileDuplicateNicknameException();

    private ProfileDuplicateNicknameException() { super(ErrorCode.PROFILE_DUPLICATE_NICKNAME); }
}