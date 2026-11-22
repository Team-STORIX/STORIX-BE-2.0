package com.storix.domain.domains.aichat.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AiChatUsageLimitExceededException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AiChatUsageLimitExceededException();

    private AiChatUsageLimitExceededException() { super(ErrorCode.AI_CHAT_USAGE_LIMIT_EXCEEDED); }
}
