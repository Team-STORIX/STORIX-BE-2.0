package com.storix.domain.domains.works.dto;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXDynamicException;
import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksIdentity;
import com.storix.domain.domains.works.domain.WorksType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record WorksImportCommand(
        WorksImportItem item,
        String worksName,
        String artistName,
        AgeClassification ageClassification,
        Genre genre,
        WorksType worksType,
        Platform platform,
        Set<String> artists
) {

    public static WorksImportCommand from(WorksImportItem item) {
        // 대상 작품을 지정하면 판정을 안 하므로 보낸 필드만 갱신
        boolean targeted = item.targetWorksId() != null;
        String worksName = targeted ? item.worksName() : requireText(item.worksName(), "worksName");
        String artistName = targeted ? item.artistName() : WorksIdentity.dedupeArtistName(requireText(item.artistName(), "artistName"));
        WorksType worksType = parse(WorksType.class, item.worksType(), "worksType");
        return new WorksImportCommand(
                item,
                worksName,
                artistName,
                parse(AgeClassification.class, item.ageClassification(), "ageClassification"),
                parse(Genre.class, item.genre(), "genre"),
                targeted ? worksType : require(worksType, "worksType"),
                parse(Platform.class, item.platform(), "platform"),
                WorksIdentity.artistNames(artistName, item.author(), item.illustrator(), item.originalAuthor())
        );
    }

    public String titleKey() {
        return WorksIdentity.titleKey(worksName);
    }

    public boolean isBookEdition() {
        return WorksIdentity.isBookEdition(worksName, worksType);
    }

    public String bookBaseKey() {
        return WorksIdentity.bookBaseKey(worksName);
    }

    public List<String> hashtagNames() {
        if (item.hashtags() == null) return List.of();
        return item.hashtags().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();
    }

    public Works toNewWorks(Set<Hashtag> hashtags) {
        Works works = Works.builder()
                .worksName(worksName)
                .artistName(artistName)
                .author(item.author())
                .illustrator(item.illustrator())
                .originalAuthor(item.originalAuthor())
                .ageClassification(require(ageClassification, "ageClassification"))
                .genre(require(genre, "genre"))
                .worksType(worksType)
                .description(requireText(item.description(), "description"))
                .thumbnailUrl(requireText(item.thumbnailUrl(), "thumbnailUrl"))
                .build();
        if (platform != null) works.putPlatform(platform, item.landingUrl());
        works.addHashtags(hashtags);
        return works;
    }

    // 해시태그는 보냈을 때만 기존에 더함
    public List<String> applyTo(Works works, Set<Hashtag> hashtags) {
        List<String> changes = new ArrayList<>(works.updateFromImport(item.author(), item.illustrator(), item.originalAuthor(),
                ageClassification, genre, worksType, item.description(), item.thumbnailUrl()));
        changes.add(platform == null ? null : works.putPlatform(platform, item.landingUrl()));
        changes.add(works.addHashtags(hashtags));
        changes.removeIf(Objects::isNull);
        return changes;
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String name, String field) {
        if (name == null || name.isBlank()) return null;
        return Arrays.stream(type.getEnumConstants())
                .filter(value -> value.name().equals(name.trim()))
                .findFirst()
                .orElseThrow(() -> new STORIXDynamicException(ErrorCode.WORKS_IMPORT_UNKNOWN_ENUM, field + " 값이 카탈로그에 없습니다: " + name, null));
    }

    private static <T> T require(T value, String field) {
        if (value == null) throw new STORIXDynamicException(ErrorCode.WORKS_IMPORT_REQUIRED_VALUE, field + " 값이 비어 있습니다", null);
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new STORIXDynamicException(ErrorCode.WORKS_IMPORT_REQUIRED_VALUE, field + " 값이 비어 있습니다", null);
        return value.trim();
    }
}
