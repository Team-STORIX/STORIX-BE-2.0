package com.storix.domain.domains.report.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownReportCaseException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownReportCaseException();

    private UnknownReportCaseException() {
        super(ErrorCode.NOT_FOUND);
    }
}
