package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksPlatform;
import com.storix.domain.domains.works.domain.Platform;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

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
        List<String> platforms,
        String ageClassification,
        Double avgRating,
        Long reviewCount,
        String description,
        List<String> hashtags,
        boolean hasTopicRoom
) {
    public static WorksDetailResponseDto from(Works works, Long reviewCount, boolean hasTopicRoom) {
        return WorksDetailResponseDto.builder()
                .worksId(works.getId())
                .worksName(works.getWorksName())
                .worksType(works.getWorksType() != null ? works.getWorksType().getDbValue() : null)
                .thumbnailUrl(works.getThumbnailUrl())
                .author(works.getAuthor())
                .illustrator(resolveIllustrator(works.getAuthor(), works.getIllustrator()))
                .originalAuthor(works.getOriginalAuthor())
                .genre(works.getGenre() != null ? works.getGenre().getDbValue() : null)
                .platforms(works.getPlatforms().stream()
                        .map(WorksPlatform::getPlatform)
                        .filter(Objects::nonNull)
                        .map(Platform::getDbValue)
                        .toList())
                .ageClassification(works.getAgeClassification() != null ? works.getAgeClassification().getDbValue() : null)
                .avgRating(roundAvgRating(works.getAvgRating()))
                .reviewCount(reviewCount)
                .description(works.getDescription())
                .hashtags(works.getHashtags().stream()
                        .map(Hashtag::getName)
                        .toList())
                .hasTopicRoom(hasTopicRoom)
                .build();
    }

    // 그림 작가와 글 작가가 동일하면 그림 작가를 내려주지 않아 하나만 표시되도록 한다.
    private static String resolveIllustrator(String author, String illustrator) {
        if (author != null && author.equals(illustrator)) {
            return null;
        }
        return illustrator;
    }

    public static Double roundAvgRating(Double avgRating) {
        return BigDecimal
                .valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
