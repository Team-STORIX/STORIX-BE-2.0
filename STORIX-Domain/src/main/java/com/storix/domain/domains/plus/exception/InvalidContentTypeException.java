
package com.storix.domain.domains.plus.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidContentTypeException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidContentTypeException();

    private InvalidContentTypeException() { super(ErrorCode.PLUS_INVALID_CONTENT_TYPE); }
}
