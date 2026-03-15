
package com.storix.domain.domains.plus.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class PlusImageNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new PlusImageNotExistException();

    private PlusImageNotExistException() { super(ErrorCode.PLUS_IMAGE_NOT_EXIST); }
}
