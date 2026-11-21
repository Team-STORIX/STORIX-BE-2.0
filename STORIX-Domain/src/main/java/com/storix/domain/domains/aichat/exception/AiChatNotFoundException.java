package com.storix.domain.domains.aichat.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AiChatNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AiChatNotFoundException();

    private AiChatNotFoundException() { super(ErrorCode.AI_CHAT_NOT_FOUND); }
}
