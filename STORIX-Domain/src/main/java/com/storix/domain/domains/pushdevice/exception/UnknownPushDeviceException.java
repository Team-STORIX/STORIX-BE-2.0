package com.storix.domain.domains.pushdevice.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownPushDeviceException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new UnknownPushDeviceException();

    private UnknownPushDeviceException() { super(ErrorCode.PUSH_DEVICE_NOT_FOUND); }
}
