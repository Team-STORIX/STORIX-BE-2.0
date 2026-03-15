package com.storix.storix_api.global.apiPayload.exception.topicRoom;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class ChatConnectionFailureException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new ChatConnectionFailureException();

    private ChatConnectionFailureException() { super(ErrorCode.CHAT_CONNECTION_ERROR); }
}
