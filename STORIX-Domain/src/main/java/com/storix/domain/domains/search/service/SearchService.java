package com.storix.domain.domains.search.service;

import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.WorksSearchResponseDto;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final WorksAdaptor worksAdaptor;

    // 작품 탭 검색: 성인 작품도 노출하고, isAdultOnly 플래그로 프론트에서 블러 처리한다
    @Transactional(readOnly = true)
    public Slice<WorksSearchResponseDto> searchWorks(Long userId, String keyword, Pageable pageable) {

        return worksAdaptor.searchWorks(keyword, false, pageable).map(this::toWorkDto);
    }

    @Transactional(readOnly = true)
    public Slice<WorksSearchResponseDto> searchWorksWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable) {

        Slice<Works> worksSlice;
        if (keyword != null && keyword.startsWith("#")) {
            // 2-1. 해시태그 검색
            String hashtagKeyword = keyword.substring(1).strip(); // # 제거
            worksSlice = worksAdaptor.searchWorksByHashtagWithFilters(hashtagKeyword, worksTypes, genres, pageable);
        } else {
            // 2-2. 작품명 검색
            worksSlice = worksAdaptor.searchWorksWithFilters(keyword, worksTypes, genres, pageable);
        }

        return worksSlice.map(this::toWorkDto);
    }

    // 피드 작성용 작품 검색: 성인 작품도 노출하고, isAdultOnly 플래그로 프론트에서 블러 처리한다
    @Transactional(readOnly = true)
    public PlusSearchResponseWrapperDto<WorksSearchResponseDto> searchWorksForWriting(Long userId, String keyword, Pageable pageable) {

        // 작품 검색
        Slice<Works> worksSlice = worksAdaptor.searchWorks(keyword, false, pageable);

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
                .avgRating(roundAvgRating(works.getAvgRating()))
                .worksType(works.getWorksType() != null ? works.getWorksType().getDbValue() : null)
                .isAdultOnly(AdultContentPolicy.isAdultOnly(works.getAgeClassification()))
                .build();
    }

    private Double roundAvgRating(Double avgRating) {
        return BigDecimal
                .valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
