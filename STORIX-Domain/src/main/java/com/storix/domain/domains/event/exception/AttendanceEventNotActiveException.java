package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AttendanceEventNotActiveException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AttendanceEventNotActiveException();

    private AttendanceEventNotActiveException() { super(ErrorCode.ATTENDANCE_EVENT_NOT_ACTIVE); }
}
