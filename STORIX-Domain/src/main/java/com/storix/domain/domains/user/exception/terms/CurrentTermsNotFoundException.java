package com.storix.domain.domains.user.exception.terms;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class CurrentTermsNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new CurrentTermsNotFoundException();

    private CurrentTermsNotFoundException() { super(ErrorCode.CURRENT_TERMS_NOT_FOUND); }
}
