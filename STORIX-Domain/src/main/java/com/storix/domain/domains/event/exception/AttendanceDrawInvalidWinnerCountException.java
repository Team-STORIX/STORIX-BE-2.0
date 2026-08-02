package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AttendanceDrawInvalidWinnerCountException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AttendanceDrawInvalidWinnerCountException();

    private AttendanceDrawInvalidWinnerCountException() { super(ErrorCode.ATTENDANCE_DRAW_INVALID_WINNER_COUNT); }
}
