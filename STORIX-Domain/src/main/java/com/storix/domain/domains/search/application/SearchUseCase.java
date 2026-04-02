package com.storix.domain.domains.search.application;

import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.WorksSearchResponseDto;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksSortType;
import com.storix.domain.domains.works.domain.WorksType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchUseCase {

    // 작품 탭 검색
    SearchResponseWrapperDto<WorksSearchResponseDto> searchWorks(Long userId, String keyword, Pageable pageable);

    // 작품 탭 필터 검색
    SearchResponseWrapperDto<WorksSearchResponseDto> searchWorksWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable);

    // [+] 탭 검색
    PlusSearchResponseWrapperDto<WorksSearchResponseDto> searchWorksForWriting(String keyword, Pageable pageable);

}