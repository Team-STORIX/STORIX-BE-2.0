package com.storix.domain.domains.report.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AlreadyProcessedReportException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AlreadyProcessedReportException();

    private AlreadyProcessedReportException() {
        super(ErrorCode.BAD_REQUEST);
    }
}
