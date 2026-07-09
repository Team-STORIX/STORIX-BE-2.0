package com.storix.common.utils;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXDynamicException;

import java.util.Arrays;

public enum ImageContentType {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String mimeType;
    private final String extension;

    ImageContentType(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    // 허용 안 되거나 null 이면 예외
    public static String toExtension(String mimeType) {
        return Arrays.stream(values())
                .filter(type -> type.mimeType.equals(mimeType))
                .findFirst()
                .orElseThrow(() -> new STORIXDynamicException(
                        ErrorCode.IMAGE_INVALID_CONTENT_TYPE, "지원하지 않는 이미지 타입: " + mimeType, null))
                .extension;
    }
}
