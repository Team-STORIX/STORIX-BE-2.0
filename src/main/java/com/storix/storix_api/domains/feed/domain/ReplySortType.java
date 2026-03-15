package com.storix.storix_api.domains.feed.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ReplySortType {

    LATEST("기본순", Sort.by(Sort.Direction.ASC, "id"));

    private final String description;
    private final Sort sortValue;
}
