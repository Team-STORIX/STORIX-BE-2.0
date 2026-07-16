package com.storix.domain.domains.user.exception.admin;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class InvalidAdminUserSanctionRequestException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new InvalidAdminUserSanctionRequestException();

    private InvalidAdminUserSanctionRequestException() {
        super(ErrorCode.INVALID_ADMIN_USER_SANCTION_REQUEST);
    }
}
