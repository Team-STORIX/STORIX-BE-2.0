package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateNicknameException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateNicknameException();

    private DuplicateNicknameException() { super(ErrorCode.ONBOARDING_DUPLICATE_NICKNAME); }
}