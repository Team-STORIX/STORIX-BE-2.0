package com.storix.domain.domains.user.exception.tester;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class TesterIdentifierMismatchException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new TesterIdentifierMismatchException();

    private TesterIdentifierMismatchException() { super(ErrorCode.TESTER_IDENTIFIER_MISMATCH); }
}
