package com.storix.storix_api.domains.search.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@Builder
public class SearchResponseWrapperDto<T> {

    private Slice<T> result;    // 무한 스크롤 데이터
    private String fallbackRecommendation;      // 검색 결과 없는 경우 추천 검색어
}
