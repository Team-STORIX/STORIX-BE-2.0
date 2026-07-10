package com.storix.domain.domains.user.exception.tester;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class TesterNotApprovedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new TesterNotApprovedException();

    private TesterNotApprovedException() { super(ErrorCode.TESTER_NOT_APPROVED); }
}
