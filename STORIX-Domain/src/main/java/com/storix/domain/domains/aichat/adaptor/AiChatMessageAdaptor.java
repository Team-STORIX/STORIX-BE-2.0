package com.storix.domain.domains.aichat.adaptor;

import com.storix.domain.domains.aichat.domain.AiChatMessage;
import com.storix.domain.domains.aichat.repository.AiChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiChatMessageAdaptor {

    private final AiChatMessageRepository aiChatMessageRepository;

    public AiChatMessage save(AiChatMessage message) {
        return aiChatMessageRepository.save(message);
    }
}
