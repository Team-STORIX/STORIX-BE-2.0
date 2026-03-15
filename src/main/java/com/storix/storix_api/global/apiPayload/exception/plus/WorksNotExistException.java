
package com.storix.storix_api.global.apiPayload.exception.plus;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class WorksNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new WorksNotExistException();

    private WorksNotExistException() { super(ErrorCode.PLUS_WORKS_NOT_EXIST); }
}
