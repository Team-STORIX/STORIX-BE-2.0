package com.storix.domain.domains.user.exception.developer;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DeveloperIdentifierMismatchException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DeveloperIdentifierMismatchException();

    private DeveloperIdentifierMismatchException() { super(ErrorCode.DEVELOPER_IDENTIFIER_MISMATCH); }
}
