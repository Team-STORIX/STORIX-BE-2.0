
package com.storix.domain.domains.user.exception.auth;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ForbiddenApproachException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ForbiddenApproachException();

    private ForbiddenApproachException() { super(ErrorCode.FORBIDDEN_APPROACH); }
}