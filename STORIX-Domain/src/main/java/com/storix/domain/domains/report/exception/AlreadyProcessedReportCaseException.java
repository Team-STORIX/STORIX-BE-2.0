package com.storix.domain.domains.report.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AlreadyProcessedReportCaseException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AlreadyProcessedReportCaseException();

    private AlreadyProcessedReportCaseException() {
        super(ErrorCode.REPORT_CASE_ALREADY_PROCESSED);
    }
}
