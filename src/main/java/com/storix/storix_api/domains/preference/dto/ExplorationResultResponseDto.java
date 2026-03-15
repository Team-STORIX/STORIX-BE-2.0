package com.storix.storix_api.domains.preference.dto;

import com.storix.storix_api.domains.works.dto.LibraryWorksInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ExplorationResultResponseDto {

    private List<LibraryWorksInfo> likedWorks;
    private List<LibraryWorksInfo> dislikedWorks;
}
