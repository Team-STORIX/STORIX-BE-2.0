
package com.storix.domain.domains.profile.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ProfileImageNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ProfileImageNotExistException();

    private ProfileImageNotExistException() { super(ErrorCode.PROFILE_IMAGE_NOT_EXIST); }
}
