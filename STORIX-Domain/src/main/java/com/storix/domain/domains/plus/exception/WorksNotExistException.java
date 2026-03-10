
package com.storix.domain.domains.plus.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class WorksNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new WorksNotExistException();

    private WorksNotExistException() { super(ErrorCode.PLUS_WORKS_NOT_EXIST); }
}
