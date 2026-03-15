package com.storix.domain.domains.search.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrendingItem {

    private String keyword;
    private int rank;
    private String status;
}
