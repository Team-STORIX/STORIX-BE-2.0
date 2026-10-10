package com.storix.domain.domains.genrescore.dto;

import com.storix.domain.domains.works.domain.Genre;

public record UserGenreKey(
        Long userId,
        Genre genre
) {
}
