package com.storix.domain.domains.works.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class WorksImportInProgressException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new WorksImportInProgressException();
    private WorksImportInProgressException() { super(ErrorCode.WORKS_IMPORT_IN_PROGRESS); }
}
