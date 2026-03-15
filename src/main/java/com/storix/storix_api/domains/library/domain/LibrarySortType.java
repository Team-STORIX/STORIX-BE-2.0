package com.storix.storix_api.domains.library.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum LibrarySortType {

    LATEST("최신순", Sort.by(Sort.Direction.DESC, "id")),
    DESC_RATING("별점 높은 순", Sort.by(Sort.Direction.DESC, "rating", "id")),
    ASC_RATING("별점 낮은 순", Sort.by(Sort.Direction.ASC, "rating", "id"));

    private final String description;
    private final Sort sortValue;
}
