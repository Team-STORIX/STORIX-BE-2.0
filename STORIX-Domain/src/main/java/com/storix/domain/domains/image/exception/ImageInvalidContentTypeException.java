package com.storix.domain.domains.image.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ImageInvalidContentTypeException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ImageInvalidContentTypeException();

    private ImageInvalidContentTypeException() { super(ErrorCode.IMAGE_INVALID_CONTENT_TYPE); }
}
