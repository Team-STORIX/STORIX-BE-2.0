


package com.storix.storix_api.global.apiPayload.exception.plus;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateReviewUploadException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateReviewUploadException();

    private DuplicateReviewUploadException() { super(ErrorCode.PLUS_DUPLICATE_REVIEW_UPLOAD); }
}
