package com.storix.storix_api.domains.review.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ReviewSortType {

    LATEST("최신순", Sort.by(Sort.Direction.DESC, "id"));

    private final String description;
    private final Sort sortValue;
}
