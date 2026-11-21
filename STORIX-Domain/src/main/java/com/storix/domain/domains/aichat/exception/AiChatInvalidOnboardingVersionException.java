package com.storix.domain.domains.aichat.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AiChatInvalidOnboardingVersionException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AiChatInvalidOnboardingVersionException();

    private AiChatInvalidOnboardingVersionException() { super(ErrorCode.AI_CHAT_INVALID_ONBOARDING_VERSION); }
}
