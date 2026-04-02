package com.storix.domain.domains.review.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ReviewSortType {

    TRENDING("좋아요순", Sort.by(Sort.Direction.DESC,"likeCount", "id"));

    private final String description;
    private final Sort sortValue;
}
