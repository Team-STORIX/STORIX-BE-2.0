
package com.storix.storix_api.global.apiPayload.exception.profile;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ProfileImageNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ProfileImageNotExistException();

    private ProfileImageNotExistException() { super(ErrorCode.PROFILE_IMAGE_NOT_EXIST); }
}
