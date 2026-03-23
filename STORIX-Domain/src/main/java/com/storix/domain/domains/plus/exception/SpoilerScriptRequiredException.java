package com.storix.domain.domains.plus.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class SpoilerScriptRequiredException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SpoilerScriptRequiredException();

    private SpoilerScriptRequiredException() { super(ErrorCode.SPOILER_SCRIPT_REQUIRED); }
}
