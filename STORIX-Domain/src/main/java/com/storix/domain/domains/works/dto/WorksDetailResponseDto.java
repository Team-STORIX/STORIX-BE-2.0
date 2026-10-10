package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksPlatform;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
        List<WorksPlatformLink> platformLinks,
        String ageClassification,
        Double avgRating,
        Long reviewCount,
        String description,
        List<String> hashtags,
        boolean hasTopicRoom
) {
    public static WorksDetailResponseDto from(Works works, Long reviewCount, boolean hasTopicRoom) {
        List<WorksPlatform> worksPlatforms = works.getPlatforms().stream()
                .filter(worksPlatform -> worksPlatform.getPlatform() != null)
                .sorted(Comparator.comparing(WorksPlatform::getPlatform))
                .toList();
        List<String> writers = splitNames(works.getOriginalAuthor(), works.getAuthor());
        boolean creditsIllustrator = works.getWorksType() == null || works.getWorksType().creditsIllustrator();
        List<String> illustrators = splitNames(creditsIllustrator ? works.getIllustrator() : null).stream()
                .filter(name -> !writers.contains(name))
                .toList();
        List<String> hashtags = works.getHashtags().stream()
                .map(Hashtag::getName)
                .toList();
        return WorksDetailResponseDto.builder()
                .worksId(works.getId())
                .worksName(works.getWorksName())
                .worksType(works.getWorksType() != null ? works.getWorksType().getDbValue() : null)
                .thumbnailUrl(works.getThumbnailUrl())
                .author(joinNames(writers))
                .illustrator(joinNames(illustrators))
                .originalAuthor(works.getOriginalAuthor())
                .genre(works.getGenre() != null ? works.getGenre().getDbValue() : null)
                .platforms(worksPlatforms.stream()
                        .map(worksPlatform -> worksPlatform.getPlatform().getDbValue())
                        .toList())
                .platformLinks(worksPlatforms.stream()
                        .map(WorksPlatformLink::from)
                        .toList())
                .ageClassification(works.getAgeClassification() != null ? works.getAgeClassification().getDbValue() : null)
                .avgRating(roundAvgRating(works.getAvgRating()))
                .reviewCount(reviewCount)
                .description(works.getDescription())
                .hashtags(works.getGenre() == Genre.BL ? sortByBlRole(hashtags) : hashtags)
                .hasTopicRoom(hasTopicRoom)
                .build();
    }

    // 원작 · 글 · 그림 순으로 이름 단위 중복 제거. 검색 · 피드의 작가 표기와 같은 순서
    private static List<String> splitNames(String... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .flatMap(value -> Stream.of(value.split(",")))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();
    }

    // BL 은 공 → 수 → 나머지, 같은 묶음은 가나다순. 두 글자 이하(복수 · 가수 등)는 공 · 수로 보지 않음
    private static List<String> sortByBlRole(List<String> hashtags) {
        return hashtags.stream()
                .sorted(Comparator.comparingInt(WorksDetailResponseDto::blRoleOrder).thenComparing(Comparator.naturalOrder()))
                .toList();
    }

    private static int blRoleOrder(String hashtag) {
        if (hashtag.length() < 3) return 2;
        if (hashtag.endsWith("공")) return 0;
        if (hashtag.endsWith("수")) return 1;
        return 2;
    }

    private static String joinNames(List<String> names) {
        return names.isEmpty() ? null : String.join(", ", names);
    }

    public static Double roundAvgRating(Double avgRating) {
        return BigDecimal
                .valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
