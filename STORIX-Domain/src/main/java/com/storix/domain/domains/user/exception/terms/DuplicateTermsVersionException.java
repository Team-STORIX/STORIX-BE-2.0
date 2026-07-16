package com.storix.domain.domains.user.exception.terms;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateTermsVersionException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateTermsVersionException();

    private DuplicateTermsVersionException() { super(ErrorCode.DUPLICATE_TERMS_VERSION); }
}
