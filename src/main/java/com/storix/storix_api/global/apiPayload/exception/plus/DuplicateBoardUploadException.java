

package com.storix.storix_api.global.apiPayload.exception.plus;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class DuplicateBoardUploadException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateBoardUploadException();

    private DuplicateBoardUploadException() { super(ErrorCode.PLUS_DUPLICATE_BOARD_UPLOAD); }
}
