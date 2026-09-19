package com.storix.common.exception;

import com.storix.common.code.ErrorCode;
import lombok.Getter;

@Getter
public class STORIXCodeException extends RuntimeException {

    public ErrorCode errorCode;

    public STORIXCodeException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }
}
