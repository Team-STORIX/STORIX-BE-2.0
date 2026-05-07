package com.storix.domain.domains.genrescore.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenreScoreEventType {

    ONBOARDING_SELECT(5),
    TOPIC_ROOM_JOIN(4),
    REVIEW_WRITE_POSITIVE(4),
    BOARD_WRITE(3),
    FAVORITE_WORKS_ADD(3);

    private final int weight;
}
