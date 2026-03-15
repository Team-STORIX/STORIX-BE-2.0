package com.storix.storix_api.domains.works.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum WorksPlusSortType {

    NAME("가나다순", Sort.by(Sort.Direction.ASC, "worksName"));

    private final String description;
    private final Sort sortValue;
}
