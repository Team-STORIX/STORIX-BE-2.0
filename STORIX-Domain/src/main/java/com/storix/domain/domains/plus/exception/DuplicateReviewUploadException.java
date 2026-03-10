


package com.storix.domain.domains.plus.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateReviewUploadException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateReviewUploadException();

    private DuplicateReviewUploadException() { super(ErrorCode.PLUS_DUPLICATE_REVIEW_UPLOAD); }
}
