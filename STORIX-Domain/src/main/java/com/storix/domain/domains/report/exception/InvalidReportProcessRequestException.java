package com.storix.domain.domains.report.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidReportProcessRequestException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidReportProcessRequestException();

    private InvalidReportProcessRequestException() {
        super(ErrorCode.INVALID_REPORT_PROCESS_REQUEST);
    }
}
