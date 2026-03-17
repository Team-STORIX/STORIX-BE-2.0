package com.storix.domain.domains.preference.dto;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksPlatform;
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

    private List<String> platforms;
    private String genre;
    private String description;
    private List<String> hashtags;

    public static ExplorationWorksResponseDto from(Works works) {
        return ExplorationWorksResponseDto.builder()
                .worksId(works.getId())
                .worksName(works.getWorksName())
                .thumbnailUrl(works.getThumbnailUrl())
                .artistName(works.getArtistName())
                .platforms(works.getPlatforms() != null ? works.getPlatforms().stream()
                        .map(wp -> wp.getPlatform().getDbValue())
                        .toList() : List.of())
                .genre(works.getGenre() != null ? works.getGenre().getDbValue() : null)
                .description(works.getDescription())
                .hashtags(works.getHashtags() != null ? works.getHashtags().stream()
                        .map(Hashtag::getName)
                        .toList() : List.of())
                .build();
    }
}
