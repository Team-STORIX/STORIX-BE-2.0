package com.storix.domain.domains.genrescore.dto;

import com.storix.domain.domains.works.domain.Genre;

import java.time.LocalDateTime;

public record RecentGenreScore(
        Genre genre,
        Long score,
        LocalDateTime latestAt
) {
}
