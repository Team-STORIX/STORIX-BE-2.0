package com.storix.domain.domains.user.exception.developer;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DeveloperNotApprovedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DeveloperNotApprovedException();

    private DeveloperNotApprovedException() { super(ErrorCode.DEVELOPER_NOT_APPROVED); }
}
