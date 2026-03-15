package com.storix.domain.domains.preference.dto;

import com.storix.domain.domains.preference.domain.PreferenceExploration;

public record ExplorationSubmitRequestDto(Long worksId, boolean isLiked) {

    public PreferenceExploration toEntity(Long userId) {
        return PreferenceExploration.builder()
                .userId(userId)
                .worksId(worksId)
                .isLiked(isLiked)
                .build();
    }
}
