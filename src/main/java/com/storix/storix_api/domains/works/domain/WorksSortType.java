package com.storix.storix_api.domains.works.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum WorksSortType {

    NAME("가나다순", Sort.by(Sort.Direction.ASC, "worksName")),
    RATING("별점 높은 순", Sort.by(Sort.Direction.DESC, "avgRating", "id")),
    REVIEW("리뷰 많은 순", Sort.by(Sort.Direction.DESC, "reviewsCount", "id"));

    private final String description;
    private final Sort sortValue;
}
