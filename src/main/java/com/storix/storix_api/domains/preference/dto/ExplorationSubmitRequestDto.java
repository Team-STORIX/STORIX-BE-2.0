package com.storix.storix_api.domains.preference.dto;

import com.storix.storix_api.domains.preference.domain.PreferenceExploration;

public record ExplorationSubmitRequestDto(Long worksId, boolean isLiked) {

    public PreferenceExploration toEntity(Long userId) {
        return PreferenceExploration.builder()
                .userId(userId)
                .worksId(worksId)
                .isLiked(isLiked)
                .build();
    }
}
