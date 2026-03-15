package com.storix.domain.domains.search.dto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorksSearchResponseDto {

    private Long worksId;
    private String worksName;
    private String artistName;
    private Long reviewsCount;  // DTO에서 Long으로
    private Double avgRating;   // DTO에서 Double로
    private String thumbnailUrl;
    private String worksType;
}
