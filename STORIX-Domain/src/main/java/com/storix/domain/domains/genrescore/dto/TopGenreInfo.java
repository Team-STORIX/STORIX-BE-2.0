package com.storix.domain.domains.genrescore.dto;

import com.storix.domain.domains.works.domain.Genre;

public record TopGenreInfo(
        Genre genre,
        long score
) {
}
