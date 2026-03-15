package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.works.domain.Works;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Builder
public record WorksDetailResponseDto(
        Long worksId,
        String worksName,
        String worksType,
        String thumbnailUrl,
        String author,
        String illustrator,
        String originalAuthor,
        String genre,
        String platform,
        String ageClassification,
        Double avgRating,
        Long reviewCount,
        String description,
        List<String> hashtags
) {
    public static WorksDetailResponseDto from(Works works, Long reviewCount) {
        return WorksDetailResponseDto.builder()
                .worksId(works.getId())
                .worksName(works.getWorksName())
                .worksType(works.getWorksType().getDbValue())
                .thumbnailUrl(works.getThumbnailUrl())
                .author(works.getAuthor())
                .illustrator(works.getIllustrator())
                .originalAuthor(works.getOriginalAuthor())
                .genre(works.getGenre().getDbValue())
                .platform(works.getPlatform().getDbValue())
                .ageClassification(works.getAgeClassification().getDbValue())
                .avgRating(works.getAvgRating() != null ? roundAvgRating(works.getAvgRating()) : 0.0)
                .reviewCount(reviewCount)
                .description(works.getDescription())
                .hashtags(works.getHashtags().stream()
                        .map(Hashtag::getName)
                        .toList())
                .build();
    }

    public static Double roundAvgRating(Double avgRating) {
        return BigDecimal
                .valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
