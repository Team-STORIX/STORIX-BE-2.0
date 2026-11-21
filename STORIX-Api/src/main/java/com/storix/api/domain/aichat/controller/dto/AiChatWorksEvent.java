package com.storix.api.domain.aichat.controller.dto;

import java.util.List;

public record AiChatWorksEvent(List<AiChatRecommendedWork> recommendedWorks, String pipelineVersion) {
}
