package com.storix.api.domain.aisearch.controller.dto;

import java.util.List;

public record AiSearchWorksEvent(List<AiSearchRecommendedWork> recommendedWorks, String pipelineVersion) {
}
