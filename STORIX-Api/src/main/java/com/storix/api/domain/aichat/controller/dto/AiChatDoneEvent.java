package com.storix.api.domain.aichat.controller.dto;

public record AiChatDoneEvent(Long chatId, Long messageId, String status) {
}
