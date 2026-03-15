
package com.storix.domain.domains.plus.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidRatingException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidRatingException();

    private InvalidRatingException() { super(ErrorCode.PLUS_INVALID_RATING); }
}
