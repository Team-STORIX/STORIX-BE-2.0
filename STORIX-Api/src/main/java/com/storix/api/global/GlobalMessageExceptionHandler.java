package com.storix.api.global;

import com.storix.common.exception.STORIXCodeException;
import com.storix.common.payload.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class GlobalMessageExceptionHandler {

    private static final String ERROR_DESTINATION = "/queue/errors";

    @MessageExceptionHandler(STORIXCodeException.class)
    @SendToUser(destinations = ERROR_DESTINATION, broadcast = false)
    public ErrorResponse handleCodeException(STORIXCodeException e, SimpMessageHeaderAccessor accessor) {
        log.warn(">>>> [STOMP] 메시지 처리 실패 sessionId={}, destination={}, code={}",
                accessor.getSessionId(),
                accessor.getDestination(),
                e.getErrorCode().getCode());

        return new ErrorResponse(e.getErrorCode());
    }

    @MessageExceptionHandler(Exception.class)
    public void handleException(Exception e, SimpMessageHeaderAccessor accessor) {
        log.error(">>>> [STOMP] 메시지 처리 실패 sessionId={}, destination={}, exceptionType={}, message={}",
                accessor.getSessionId(),
                accessor.getDestination(),
                e.getClass().getSimpleName(),
                e.getMessage());
    }
}
