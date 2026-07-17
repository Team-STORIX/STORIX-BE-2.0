package com.storix.domain.domains.report.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidReportActionCombinationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidReportActionCombinationException();

    private InvalidReportActionCombinationException() { super(ErrorCode.INVALID_REPORT_ACTION_COMBINATION); }
}
