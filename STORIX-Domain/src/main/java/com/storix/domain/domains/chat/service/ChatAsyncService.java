package com.storix.domain.domains.chat.service;

import com.storix.domain.domains.chat.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatAsyncService {

    private final ChatMessageSentHelper chatMessageSentHelper;

    // 저장과 발행은 이미 끝난 상태로 들어온다
    @Async("chatAsyncExecutor")
    public void processAfterMessageSent(ChatMessage savedMessage, String senderNickname) {
        chatMessageSentHelper.apply(savedMessage, senderNickname);
    }
}
