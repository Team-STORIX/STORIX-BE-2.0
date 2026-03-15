

package com.storix.domain.domains.plus.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class DuplicateBoardUploadException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new DuplicateBoardUploadException();

    private DuplicateBoardUploadException() { super(ErrorCode.PLUS_DUPLICATE_BOARD_UPLOAD); }
}
