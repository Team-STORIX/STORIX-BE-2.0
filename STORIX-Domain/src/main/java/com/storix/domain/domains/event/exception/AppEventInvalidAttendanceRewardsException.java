package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AppEventInvalidAttendanceRewardsException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventInvalidAttendanceRewardsException();

    private AppEventInvalidAttendanceRewardsException() { super(ErrorCode.ADMIN_APP_EVENT_INVALID_ATTENDANCE_REWARDS); }
}
