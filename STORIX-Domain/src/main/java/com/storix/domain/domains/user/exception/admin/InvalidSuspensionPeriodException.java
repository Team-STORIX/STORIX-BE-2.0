package com.storix.domain.domains.user.exception.admin;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidSuspensionPeriodException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidSuspensionPeriodException();

    private InvalidSuspensionPeriodException() { super(ErrorCode.INVALID_SUSPENSION_PERIOD); }
}
