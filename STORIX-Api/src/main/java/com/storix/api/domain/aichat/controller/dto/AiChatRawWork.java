package com.storix.api.domain.aichat.controller.dto;


import java.util.List;

public record AiChatRawWork(
        int rank,
        Long workId,
        String reason,
        List<AiChatMatchedClue> matchedClues
) {
}
