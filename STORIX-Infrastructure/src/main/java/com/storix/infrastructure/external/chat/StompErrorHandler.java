package com.storix.infrastructure.external.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.code.ErrorCode;
import com.storix.common.code.StompErrorReason;
import com.storix.common.exception.STORIXCodeException;
import com.storix.common.payload.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            @Nullable Message<byte[]> clientMessage, Throwable ex) {

        ErrorCode errorCode = resolveErrorCode(ex);

        if (errorCode == null) {
            logUnexpected(clientMessage, ex);
            return errorFrame(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        logRejection(clientMessage, errorCode);

        return errorFrame(errorCode);
    }

    private ErrorCode resolveErrorCode(Throwable ex) {
        Throwable cause = ex;

        while (cause != null) {
            if (cause instanceof STORIXCodeException codeException) {
                return codeException.getErrorCode();
            }
            if (cause == cause.getCause()) {
                break;
            }
            cause = cause.getCause();
        }

        return null;
    }

    private void logRejection(@Nullable Message<byte[]> clientMessage, ErrorCode errorCode) {
        if (clientMessage == null) {
            log.warn(">>>> [STOMP] 프레임 거절 code={}", errorCode.getCode());
            return;
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(clientMessage);
        log.warn(">>>> [STOMP] 프레임 거절 sessionId={}, command={}, destination={}, code={}",
                accessor.getSessionId(),
                accessor.getCommand(),
                accessor.getDestination(),
                errorCode.getCode());
    }

    private void logUnexpected(@Nullable Message<byte[]> clientMessage, Throwable ex) {
        if (clientMessage == null) {
            log.warn(">>>> [STOMP] 프레임 처리 실패 cause={}", ex.toString());
        } else {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(clientMessage);
            log.warn(">>>> [STOMP] 프레임 처리 실패 sessionId={}, command={}, destination={}, cause={}",
                    accessor.getSessionId(),
                    accessor.getCommand(),
                    accessor.getDestination(),
                    ex.toString());
        }

        log.debug(">>>> [STOMP] 프레임 처리 실패 상세", ex);
    }

    private Message<byte[]> errorFrame(ErrorCode errorCode) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(StompErrorReason.from(errorCode).name());
        accessor.setContentType(MimeTypeUtils.APPLICATION_JSON);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(toPayload(errorCode), accessor.getMessageHeaders());
    }

    private byte[] toPayload(ErrorCode errorCode) {
        try {
            return objectMapper.writeValueAsBytes(new ErrorResponse(errorCode));
        } catch (Exception e) {
            log.warn(">>>> [STOMP] 에러 본문 직렬화 실패 code={}, cause={}", errorCode.getCode(), e.getMessage());
            return new byte[0];
        }
    }
}
