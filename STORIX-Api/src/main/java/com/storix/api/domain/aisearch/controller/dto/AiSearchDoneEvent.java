package com.storix.api.domain.aisearch.controller.dto;

public record AiSearchDoneEvent(Long chatId, Long messageId, String status) {
}
