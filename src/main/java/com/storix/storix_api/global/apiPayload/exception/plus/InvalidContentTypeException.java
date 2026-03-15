
package com.storix.storix_api.global.apiPayload.exception.plus;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidContentTypeException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidContentTypeException();

    private InvalidContentTypeException() { super(ErrorCode.PLUS_INVALID_CONTENT_TYPE); }
}
