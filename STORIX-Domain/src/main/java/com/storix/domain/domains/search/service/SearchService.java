package com.storix.domain.domains.search.service;

import com.storix.domain.domains.search.application.SearchUseCase;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.WorksSearchResponseDto;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.domain.Works;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class SearchService implements SearchUseCase {

    private final LoadWorksPort loadWorksPort;
    private final SearchHistoryService searchHistoryService;

    @Override
    @Transactional
    public SearchResponseWrapperDto<WorksSearchResponseDto> searchWorks(Long userId, String keyword, Pageable pageable) {

        // 1. 검색어 저장
        if (keyword != null && pageable.getPageNumber() == 0) {
            searchHistoryService.addSearchLog(userId, keyword);
        }

        // 2. 작품 조회
        Slice<Works> worksSlice = loadWorksPort.searchWorks(keyword, pageable);

        // 3. 결과 없으면 추천 검색어 조회
        String fallbackKeyword = worksSlice.isEmpty() ? searchHistoryService.getFallbackRecommendation() : null;

        return SearchResponseWrapperDto.<WorksSearchResponseDto>builder()
                .result(worksSlice.map(this::toWorkDto))
                .fallbackRecommendation(fallbackKeyword)
                .build();
    }

    @Override
    @Transactional
    public PlusSearchResponseWrapperDto<WorksSearchResponseDto> searchWorksForWriting(String keyword, Pageable pageable) {

        // 작품 검색
        Slice<Works> worksSlice = loadWorksPort.searchWorks(keyword, pageable);

        return PlusSearchResponseWrapperDto.<WorksSearchResponseDto>builder()
                .result(worksSlice.map(this::toWorkDto))
                .build();
    }

    private WorksSearchResponseDto toWorkDto(Works works) {
        return WorksSearchResponseDto.builder()
                .worksId(works.getId())
                .worksName(works.getWorksName())
                .artistName(works.getArtistName())
                .thumbnailUrl(works.getThumbnailUrl())
                .reviewsCount(works.getReviewsCount() != null ? works.getReviewsCount() : 0L)
                .avgRating(works.getAvgRating() != null ? roundAvgRating(works.getAvgRating()) : 0.0)
                .worksType(works.getWorksType() != null ? works.getWorksType().getDbValue() : null)
                .build();
    }

    private Double roundAvgRating(Double avgRating) {
        return BigDecimal
                .valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
