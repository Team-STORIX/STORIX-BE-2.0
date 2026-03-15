package com.storix.domain.domains.chat.application.port;

import com.storix.domain.domains.chat.domain.ChatMessage;

public interface RecordChatPort {
    ChatMessage saveMessage(ChatMessage message);
}
