package com.storix.domain.domains.appversion.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BlockedAppVersionException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BlockedAppVersionException();

    private BlockedAppVersionException() { super(ErrorCode.BLOCKED_APP_VERSION); }
}
