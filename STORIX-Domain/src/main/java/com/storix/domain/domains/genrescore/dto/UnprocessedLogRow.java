package com.storix.domain.domains.genrescore.dto;

import com.storix.domain.domains.works.domain.Genre;

public record UnprocessedLogRow(Long id, Long userId, Genre genre, Integer weight) {}
