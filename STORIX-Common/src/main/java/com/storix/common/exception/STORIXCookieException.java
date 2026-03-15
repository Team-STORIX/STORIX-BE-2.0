package com.storix.common.exception;

import com.storix.common.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class STORIXCookieException extends RuntimeException {
    public ErrorCode errorCode;
}
