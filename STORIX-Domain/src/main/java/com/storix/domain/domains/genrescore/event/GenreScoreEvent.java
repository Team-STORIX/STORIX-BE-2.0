package com.storix.domain.domains.genrescore.event;

import com.storix.domain.domains.works.domain.Genre;

public record GenreScoreEvent(
        Long userId,
        Long worksId,
        Genre genre,
        GenreScoreEventType type
) {

    public static GenreScoreEvent of(Long userId, Long worksId, Genre genre, GenreScoreEventType type) {
        return new GenreScoreEvent(userId, worksId, genre, type);
    }
}
