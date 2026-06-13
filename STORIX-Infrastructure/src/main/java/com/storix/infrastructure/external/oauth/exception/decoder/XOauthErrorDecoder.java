package com.storix.infrastructure.external.oauth.exception.decoder;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.infrastructure.external.oauth.exception.dto.XOauthErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XOauthErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("[X] OAuth 에러 응답 수신: methodKey={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        XOauthErrorResponse body = XOauthErrorResponse.from(response);
        log.error("[X] OAuth 에러 본문: error={}, errorDescription={}",
                body.error(), body.errorDescription());

        String error = body.error();
        String desc = body.errorDescription();
        if (error == null) {
            return new STORIXCodeException(ErrorCode.XOE_INVALID_REQUEST);
        }

        // X는 인가 코드 만료/무효/재사용도 invalid_request로 내려주므로 description으로 구분
        boolean isAuthCodeProblem = desc != null && desc.toLowerCase().contains("authorization code");

        return switch (error) {
            case "invalid_client", "unauthorized_client" -> {
                log.error("[X] {}: client_id/client_secret(Basic 인증) 검증 실패 가능성", error);
                yield new STORIXCodeException(ErrorCode.XOE_UNAUTHORIZED_CLIENT);
            }
            case "invalid_grant" -> {
                log.error("[X] invalid_grant: 인가 코드 만료/재사용, redirect_uri 또는 code_verifier 불일치 가능성");
                yield new STORIXCodeException(ErrorCode.XOE_INVALID_GRANT);
            }
            case "invalid_request" -> {
                if (isAuthCodeProblem) {
                    log.error("[X] invalid_request: 인가 코드 만료/무효/재사용 (유효시간 약 30초)");
                    yield new STORIXCodeException(ErrorCode.XOE_INVALID_GRANT);
                }
                log.error("[X] invalid_request: 파라미터 누락/형식 오류 가능성");
                yield new STORIXCodeException(ErrorCode.XOE_INVALID_REQUEST);
            }
            default -> {
                log.error("[X] 처리되지 않은 error 타입: {}", error);
                yield new STORIXCodeException(ErrorCode.XOE_INVALID_REQUEST);
            }
        };
    }
}
