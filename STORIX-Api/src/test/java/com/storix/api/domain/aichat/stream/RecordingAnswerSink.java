package com.storix.api.domain.aichat.stream;

import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;

import java.util.ArrayList;
import java.util.List;

class RecordingAnswerSink implements AiChatAnswerSink {

    final List<String> events = new ArrayList<>();

    @Override
    public void progress(String step, String message) {
        events.add("progress:" + step + ":" + message);
    }

    @Override
    public void token(String text) {
        events.add("token:" + text);
    }

    @Override
    public void works(List<AiChatRawWork> works, String pipelineVersion) {
        events.add("works:" + pipelineVersion + ":" + works.stream().map(w -> w.rank() + "=" + w.workId()).toList());
    }

    @Override
    public void done(AiChatAnswerStatus status) {
        events.add("done:" + status);
    }

    @Override
    public void error(String code, String message) {
        events.add("error:" + code + ":" + message);
    }
}
