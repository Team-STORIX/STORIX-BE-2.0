package com.storix.storix_api.domains.search.application.usecase;

import com.storix.storix_api.domains.search.dto.ArtistSearchResponseDto;
import com.storix.storix_api.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.storix_api.domains.search.dto.SearchResponseWrapperDto;
import com.storix.storix_api.domains.search.dto.WorksSearchResponseDto;
import org.springframework.data.domain.Pageable;

public interface SearchUseCase {

    // 작품 탭 검색
    SearchResponseWrapperDto<WorksSearchResponseDto> searchWorks(Long userId, String keyword, Pageable pageable);

    // 작가 탭 검색
    SearchResponseWrapperDto<ArtistSearchResponseDto> searchArtists(String keyword, Pageable pageable);

    // [+] 탭 검색
    PlusSearchResponseWrapperDto<WorksSearchResponseDto> searchWorksForWriting(String keyword, Pageable pageable);

}