package com.storix.infrastructure.external.oauth.exception.decoder;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.infrastructure.external.oauth.exception.dto.AppleOauthErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;

public class AppleOauthErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        AppleOauthErrorResponse body = AppleOauthErrorResponse.from(response);

        return switch (body.error()) {
            case "invalid_client" -> new STORIXCodeException(ErrorCode.AOE_INVALID_CLIENT);
            case "invalid_grant" -> new STORIXCodeException(ErrorCode.AOE_INVALID_GRANT);
            default -> new STORIXCodeException(ErrorCode.AOE_INVALID_REQUEST);
        };
    }
}
