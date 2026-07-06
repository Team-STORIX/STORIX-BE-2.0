package com.storix.infrastructure.external.notification.exception;

import com.google.firebase.messaging.MessagingErrorCode;
import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXDynamicException;


public class FcmTransientException extends STORIXDynamicException {

    private final transient MessagingErrorCode messagingErrorCode;

    public FcmTransientException(MessagingErrorCode messagingErrorCode, Throwable cause) {
        super(ErrorCode.FCM_TRANSIENT_FAILURE, "FCM transient failure: " + messagingErrorCode, cause);
        this.messagingErrorCode = messagingErrorCode;
    }

    public MessagingErrorCode getMessagingErrorCode() {
        return messagingErrorCode;
    }
}
