package com.storix.api.domain.aichat.helper;

import com.storix.api.domain.aichat.controller.dto.AiChatQuestion;

public interface AiChatResponder {

    void respond(AiChatQuestion question, AiChatAnswerSink sink) throws InterruptedException;
}
