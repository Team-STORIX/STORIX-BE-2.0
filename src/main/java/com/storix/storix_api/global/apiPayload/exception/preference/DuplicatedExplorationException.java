package com.storix.storix_api.global.apiPayload.exception.preference;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicatedExplorationException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicatedExplorationException();

    private DuplicatedExplorationException() {
        super(ErrorCode.PREFERENCE_ALREADY_DONE_TODAY);
    }
}
