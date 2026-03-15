package com.storix.storix_api.domains.profile.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ProfileSortType {

    LATEST("최신순", Sort.by(Sort.Direction.DESC, "id"));

    private final String description;
    private final Sort sortValue;
}
