package com.storix.domain.domains.aichat.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class AiChatSettingNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AiChatSettingNotFoundException();

    private AiChatSettingNotFoundException() { super(ErrorCode.AI_CHAT_SETTING_NOT_FOUND); }
}
