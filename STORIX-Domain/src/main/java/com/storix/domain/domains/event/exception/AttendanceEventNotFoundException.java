package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AttendanceEventNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AttendanceEventNotFoundException();

    private AttendanceEventNotFoundException() { super(ErrorCode.ATTENDANCE_EVENT_NOT_FOUND); }
}
