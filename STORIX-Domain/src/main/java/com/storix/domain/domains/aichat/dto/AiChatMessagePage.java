package com.storix.domain.domains.aichat.dto;

import java.util.List;

// messages 는 오래된 순. hasNext 면 맨 앞 messageId 를 before 로 넘겨 더 이전을 부른다
public record AiChatMessagePage(List<AiChatMessageInfo> messages, boolean hasNext) {
}
