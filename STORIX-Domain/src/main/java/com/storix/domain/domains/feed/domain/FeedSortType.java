package com.storix.domain.domains.feed.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum FeedSortType {

    LATEST("최신순", Sort.by(Sort.Direction.DESC, "id"));

    private final String description;
    private final Sort sortValue;
}
