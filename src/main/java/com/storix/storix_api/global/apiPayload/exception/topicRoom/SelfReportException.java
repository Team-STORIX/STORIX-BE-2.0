package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class SelfReportException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SelfReportException();

    private SelfReportException() {
        super(ErrorCode.SELF_REPORT_ERROR);
    }
}
