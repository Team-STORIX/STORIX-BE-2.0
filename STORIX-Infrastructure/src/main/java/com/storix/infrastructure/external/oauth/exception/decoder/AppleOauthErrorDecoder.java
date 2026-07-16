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
        AppleOauthErrorResponse body = AppleOauthErrorResponse.from(response);

        String error = body.error();
        String desc = body.errorDescription();
        if (error == null) {
            log.warn("[Apple] OAuth 에러 응답(error 없음): methodKey={}, status={}, reason={}",
                    methodKey, response.status(), response.reason());
            return new STORIXCodeException(ErrorCode.AOE_INVALID_REQUEST);
        }

        return switch (error) {
            case "invalid_client" -> {
                log.error("[Apple] invalid_client: client_secret JWT(clientId/teamId/keyId/privateKey) 검증 실패 가능성. status={}, desc={}",
                        response.status(), desc);
                yield new STORIXCodeException(ErrorCode.AOE_INVALID_CLIENT);
            }
            case "invalid_grant" -> {
                log.warn("[Apple] invalid_grant: 인가 코드 만료/재사용/redirect_uri 불일치 가능성. desc={}", desc);
                yield new STORIXCodeException(ErrorCode.AOE_INVALID_GRANT);
            }
            default -> {
                log.error("[Apple] 처리되지 않은 error 타입: {}, status={}, desc={}", error, response.status(), desc);
                yield new STORIXCodeException(ErrorCode.AOE_INVALID_REQUEST);
            }
        };
    }
}
