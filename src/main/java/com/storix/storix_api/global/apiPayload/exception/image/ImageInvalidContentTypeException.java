package com.storix.storix_api.global.apiPayload.exception.image;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ImageInvalidContentTypeException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ImageInvalidContentTypeException();

    private ImageInvalidContentTypeException() { super(ErrorCode.IMAGE_INVALID_CONTENT_TYPE); }
}
