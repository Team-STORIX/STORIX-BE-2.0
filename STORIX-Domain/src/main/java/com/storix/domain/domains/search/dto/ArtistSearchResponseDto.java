package com.storix.domain.domains.search.dto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArtistSearchResponseDto {

    private Long artistId;
    private String artistName;
    private String profileUrl;
}
