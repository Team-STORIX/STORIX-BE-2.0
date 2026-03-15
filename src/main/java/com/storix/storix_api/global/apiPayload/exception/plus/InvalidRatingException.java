
package com.storix.storix_api.global.apiPayload.exception.plus;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidRatingException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidRatingException();

    private InvalidRatingException() { super(ErrorCode.PLUS_INVALID_RATING); }
}
