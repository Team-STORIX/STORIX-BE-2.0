package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AttendanceAlreadyCheckedInException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AttendanceAlreadyCheckedInException();

    private AttendanceAlreadyCheckedInException() { super(ErrorCode.ATTENDANCE_ALREADY_CHECKED_IN); }
}
