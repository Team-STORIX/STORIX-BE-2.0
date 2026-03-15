package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class SelfReportException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SelfReportException();

    private SelfReportException() {
        super(ErrorCode.SELF_REPORT_ERROR);
    }
}
