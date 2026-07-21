package com.storix.infrastructure.external.chat;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
public class StompMdcInterceptor implements ExecutorChannelInterceptor {

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        if (!(handler instanceof SimpAnnotationMethodMessageHandler)) return message;

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        MDC.put(STORIXStatic.Mdc.TRACE_ID, UUID.randomUUID().toString().replace("-", ""));

        String destination = accessor.getDestination();
        if (destination != null) MDC.put(STORIXStatic.Mdc.ENDPOINT, destination);

        Principal user = accessor.getUser();
        if (user instanceof Authentication auth && auth.getPrincipal() instanceof AuthUserDetails details) {
            MDC.put(STORIXStatic.Mdc.USER_ID, String.valueOf(details.getUserId()));
            MDC.put(STORIXStatic.Mdc.ROLE, details.getRole().getStringValue());
        }
        return message;
    }

    @Override
    public void afterMessageHandled(
            Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex
    ) {
        if (!(handler instanceof SimpAnnotationMethodMessageHandler)) return;

        MDC.remove(STORIXStatic.Mdc.TRACE_ID);
        MDC.remove(STORIXStatic.Mdc.ENDPOINT);
        MDC.remove(STORIXStatic.Mdc.USER_ID);
        MDC.remove(STORIXStatic.Mdc.ROLE);
    }
}
