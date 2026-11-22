package com.storix.api.domain.aichat.helper;

import com.storix.api.domain.aichat.controller.dto.AiChatRawWork;
import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;

import java.util.List;

public interface AiChatAnswerSink {

    // 순서: progress → token 여러 번 → works → done. 실패하면 error
    void progress(String step, String message);

    void token(String text);

    void works(List<AiChatRawWork> works, String pipelineVersion);

    void done(AiChatAnswerStatus status);

    void error(String code, String message);
}
