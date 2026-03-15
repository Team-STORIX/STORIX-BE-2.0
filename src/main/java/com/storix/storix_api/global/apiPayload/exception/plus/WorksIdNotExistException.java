
package com.storix.storix_api.global.apiPayload.exception.plus;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class WorksIdNotExistException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new WorksIdNotExistException();

    private WorksIdNotExistException() { super(ErrorCode.PLUS_WORKS_ID_NOT_EXIST); }
}
