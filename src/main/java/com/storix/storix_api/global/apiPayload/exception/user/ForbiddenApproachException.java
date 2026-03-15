
package com.storix.storix_api.global.apiPayload.exception.user;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ForbiddenApproachException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ForbiddenApproachException();

    private ForbiddenApproachException() { super(ErrorCode.FORBIDDEN_APPROACH); }
}