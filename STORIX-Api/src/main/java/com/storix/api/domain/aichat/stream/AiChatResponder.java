package com.storix.api.domain.aichat.stream;

// 질문에 답을 만든다. 지금은 mock, AI 서버가 준비되면 AI 서버를 부르는 구현으로 바꾼다
public interface AiChatResponder {

    void respond(AiChatQuestion question, AiChatAnswerSink sink) throws InterruptedException;
}
