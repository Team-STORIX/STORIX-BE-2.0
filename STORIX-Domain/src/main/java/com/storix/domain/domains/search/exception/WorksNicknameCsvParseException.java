package com.storix.domain.domains.search.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class WorksNicknameCsvParseException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new WorksNicknameCsvParseException();
    private WorksNicknameCsvParseException() { super(ErrorCode.WORKS_NICKNAME_CSV_PARSE_ERROR); }
}
