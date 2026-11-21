package com.storix.domain.domains.aichat.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AiChatUnsupportedWorksTypeException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AiChatUnsupportedWorksTypeException();

    private AiChatUnsupportedWorksTypeException() { super(ErrorCode.AI_CHAT_UNSUPPORTED_WORKS_TYPE); }
}
