package com.storix.infrastructure.external.oauth.exception.decoder;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.infrastructure.external.oauth.exception.dto.AppleOauthErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppleOauthErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("[Apple] OAuth 에러 응답 수신: methodKey={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        AppleOauthErrorResponse body = AppleOauthErrorResponse.from(response);
        log.error("[Apple] OAuth 에러 본문: error={}, errorDescription={}",
                body.error(), body.errorDescription());

        return switch (body.error()) {
            case "invalid_client" -> {
                log.error("[Apple] invalid_client: client_secret JWT(clientId/teamId/keyId/privateKey) 검증 실패 가능성");
                yield new STORIXCodeException(ErrorCode.AOE_INVALID_CLIENT);
            }
            case "invalid_grant" -> {
                log.error("[Apple] invalid_grant: 인가 코드 만료/재사용/redirect_uri 불일치 가능성");
                yield new STORIXCodeException(ErrorCode.AOE_INVALID_GRANT);
            }
            default -> {
                log.error("[Apple] 처리되지 않은 error 타입: {}", body.error());
                yield new STORIXCodeException(ErrorCode.AOE_INVALID_REQUEST);
            }
        };
    }
}
