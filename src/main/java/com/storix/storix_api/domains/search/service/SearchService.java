package com.storix.storix_api.domains.search.service;

import com.storix.storix_api.domains.search.application.usecase.SearchUseCase;
import com.storix.storix_api.domains.search.dto.ArtistSearchResponseDto;
import com.storix.storix_api.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.storix_api.domains.search.dto.SearchResponseWrapperDto;
import com.storix.storix_api.domains.search.dto.WorksSearchResponseDto;
import com.storix.storix_api.domains.user.application.port.LoadUserPort;
import com.storix.storix_api.domains.user.domain.User;
import com.storix.storix_api.domains.works.application.port.LoadWorksPort;
import com.storix.storix_api.domains.works.domain.Works;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class SearchService implements SearchUseCase {

    @Value("AWS_S3_BASE_URL") String baseUrl;

    private final LoadWorksPort loadWorksPort;
    private final LoadUserPort loadUserPort;
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
    public SearchResponseWrapperDto<ArtistSearchResponseDto> searchArtists(String keyword, Pageable pageable) {

        // User 테이블에서 ARTIST 권한을 가진 유저 검색
        Slice<User> artistSlice = loadUserPort.searchArtists(keyword, pageable);

        String fallback = null;
        if (artistSlice.isEmpty()) {
            fallback = searchHistoryService.getFallbackRecommendation();
        }

        // User 엔티티 -> DTO 변환
        Slice<ArtistSearchResponseDto> resultDto = artistSlice.map(this::toArtistDto);

        return SearchResponseWrapperDto.<ArtistSearchResponseDto>builder()
                .result(resultDto)
                .fallbackRecommendation(fallback)
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

    private ArtistSearchResponseDto toArtistDto(User user) {
        return ArtistSearchResponseDto.builder()
                .artistId(user.getId())
                .artistName(user.getNickName())
                .profileUrl(user.getProfileImageUrl() == null ? null : baseUrl + "/" +user.getProfileImageUrl())
                .build();
    }

    private Double roundAvgRating(Double avgRating) {
        return BigDecimal
                .valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
