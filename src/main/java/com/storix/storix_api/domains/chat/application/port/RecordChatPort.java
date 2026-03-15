package com.storix.storix_api.domains.chat.application.port;

import com.storix.storix_api.domains.chat.domain.ChatMessage;

public interface RecordChatPort {
    ChatMessage saveMessage(ChatMessage message);
}
