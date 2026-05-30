package com.storix.domain.domains.report.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidReportProcessCombinationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidReportProcessCombinationException();

    private InvalidReportProcessCombinationException() {
        super(ErrorCode.BAD_REQUEST);
    }
}
