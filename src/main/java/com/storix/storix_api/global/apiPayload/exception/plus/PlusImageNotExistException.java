
package com.storix.storix_api.global.apiPayload.exception.plus;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class PlusImageNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new PlusImageNotExistException();

    private PlusImageNotExistException() { super(ErrorCode.PLUS_IMAGE_NOT_EXIST); }
}
