package com.storix.domain.domains.image.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ImageUploadFailedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ImageUploadFailedException();

    private ImageUploadFailedException() { super(ErrorCode.IMAGE_UPLOAD_FAILED); }
}
