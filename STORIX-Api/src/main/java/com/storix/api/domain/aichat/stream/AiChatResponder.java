package com.storix.api.domain.aichat.stream;

public interface AiChatResponder {

    void respond(AiChatQuestion question, AiChatAnswerSink sink) throws InterruptedException;
}
