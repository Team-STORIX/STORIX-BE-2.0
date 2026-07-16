package com.storix.domain.domains.user.exception.admin;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AdminIdentifierMismatchException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AdminIdentifierMismatchException();

    private AdminIdentifierMismatchException() { super(ErrorCode.ADMIN_IDENTIFIER_MISMATCH); }
}
