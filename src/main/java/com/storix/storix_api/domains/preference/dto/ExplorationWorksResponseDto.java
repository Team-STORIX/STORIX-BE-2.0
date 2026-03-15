package com.storix.storix_api.domains.preference.dto;

import com.storix.storix_api.domains.hashtag.domain.Hashtag;
import com.storix.storix_api.domains.works.domain.Works;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ExplorationWorksResponseDto {

    private Long worksId;
    private String worksName;
    private String thumbnailUrl;
    private String artistName;

    private String platform;
    private String genre;
    private String description;
    private List<String> hashtags;

    public static ExplorationWorksResponseDto from(Works works) {
        return ExplorationWorksResponseDto.builder()
                .worksId(works.getId())
                .worksName(works.getWorksName())
                .thumbnailUrl(works.getThumbnailUrl())
                .artistName(works.getArtistName())
                .platform(works.getPlatform() != null ? works.getPlatform().toString() : null)
                .genre(works.getGenre() != null ? works.getGenre().getDbValue() : null)
                .description(works.getDescription())
                .hashtags(works.getHashtags() != null ? works.getHashtags().stream()
                        .map(Hashtag::getName)
                        .toList() : List.of())
                .build();
    }
}
