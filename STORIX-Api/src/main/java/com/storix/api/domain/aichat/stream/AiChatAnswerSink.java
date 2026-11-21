package com.storix.api.domain.aichat.stream;

import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;

import java.util.List;

// 답변을 만드는 쪽이 이벤트를 순서대로 넘기는 곳. progress → token(여러 번) → works → done, 실패하면 error
public interface AiChatAnswerSink {

    void progress(String step, String message);

    void token(String text);

    void works(List<AiChatRawWork> works, String pipelineVersion);

    void done(AiChatAnswerStatus status);

    void error(String code, String message);
}
