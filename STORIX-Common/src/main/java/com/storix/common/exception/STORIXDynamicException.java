package com.storix.common.exception;

import com.storix.common.code.ErrorCode;

public class STORIXDynamicException extends STORIXCodeException {

    private final String detail;

    public STORIXDynamicException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode);
        this.detail = detail;
        if (cause != null) {
            initCause(cause);
        }
    }

    @Override
    public String getMessage() {
        return detail;
    }
}
