package com.storix.domain.domains.topicroom.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class ChatConnectionFailureException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ChatConnectionFailureException();

    private ChatConnectionFailureException() { super(ErrorCode.CHAT_CONNECTION_ERROR); }
}
