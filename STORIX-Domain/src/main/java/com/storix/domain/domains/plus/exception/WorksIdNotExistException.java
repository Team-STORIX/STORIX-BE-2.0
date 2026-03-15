
package com.storix.domain.domains.plus.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class WorksIdNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new WorksIdNotExistException();

    private WorksIdNotExistException() { super(ErrorCode.PLUS_WORKS_ID_NOT_EXIST); }
}
