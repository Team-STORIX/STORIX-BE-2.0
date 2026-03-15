package com.storix.domain.domains.search.application;

import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.WorksSearchResponseDto;
import org.springframework.data.domain.Pageable;

public interface SearchUseCase {

    // 작품 탭 검색
    SearchResponseWrapperDto<WorksSearchResponseDto> searchWorks(Long userId, String keyword, Pageable pageable);

    // [+] 탭 검색
    PlusSearchResponseWrapperDto<WorksSearchResponseDto> searchWorksForWriting(String keyword, Pageable pageable);

}