package com.storix.domain.domains.preference.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicatedExplorationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicatedExplorationException();

    private DuplicatedExplorationException() {
        super(ErrorCode.PREFERENCE_ALREADY_DONE_TODAY);
    }
}
