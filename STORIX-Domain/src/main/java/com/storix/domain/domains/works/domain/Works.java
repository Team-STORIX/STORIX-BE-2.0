package com.storix.domain.domains.works.domain;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXDynamicException;
import com.storix.domain.domains.hashtag.domain.Hashtag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "works",
        indexes = {
                @Index(name = "idx_works_author", columnList = "author"),
                @Index(name = "idx_works_illustrator", columnList = "illustrator"),
                @Index(name = "idx_works_original_author", columnList = "original_author"),
                @Index(name = "idx_works_normalized_name_type", columnList = "normalized_name, works_type")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Works {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "works_id")
    private Long id;

    @OneToMany(mappedBy = "works", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorksPlatform> platforms = new HashSet<>();

    // 작품명
    @Column(name = "works_name", nullable = false)
    private String worksName;

    @Column(name = "normalized_name")
    private String normalizedName;

    // 전체 작가
    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(length = 100)
    private String author;

    @Column(length = 100)
    private String illustrator;

    @Column(name = "original_author", length = 100)
    private String originalAuthor;

    // 연령 등급
    @Column(name = "age_classification", nullable = false)
    private AgeClassification ageClassification;

    // 작품 소개
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // 작품 장르
    @Column(nullable = false)
    private Genre genre;

    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;

    @Column(name = "reviews_count")
    private Integer reviewsCount;

    @Column(name = "avg_rating", nullable = false)
    @ColumnDefault("0")
    private double avgRating;

    @Column(name = "works_type", nullable = false)
    private WorksType worksType;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "works_hashtag",
            joinColumns = @JoinColumn(name = "works_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags = new HashSet<>();

    @Column(name = "is_onboarding")
    private Boolean isOnboarding;

    @Column(name = "is_story_card_lucky_work")
    private Boolean isStoryCardLuckyWork;

    @Builder
    private Works(String worksName,
                  String artistName, String author, String illustrator,
                  String originalAuthor, AgeClassification ageClassification,
                  String description, Genre genre, String thumbnailUrl,
                  WorksType worksType) {

        this.worksName = worksName;
        this.normalizedName = WorksIdentity.titleKey(worksName);
        this.artistName = artistName;
        this.author = author;
        this.illustrator = illustrator;
        this.originalAuthor = originalAuthor;
        this.ageClassification = ageClassification;
        this.description = description;
        this.genre = genre;
        this.thumbnailUrl = thumbnailUrl;
        this.worksType = worksType;
        this.reviewsCount = 0;
        this.avgRating = 0.0;
        this.isOnboarding = false;
        this.platforms = new HashSet<>();
        this.hashtags = new HashSet<>();
    }

    public void refreshNormalizedName() {
        this.normalizedName = WorksIdentity.titleKey(worksName);
    }

    public void rename(String currentName, String newName) {
        if (!worksName.equals(currentName)) {
            throw new STORIXDynamicException(ErrorCode.WORKS_RENAME_CONFLICT, "현재 작품명: " + worksName, null);
        }
        this.worksName = newName;
        refreshNormalizedName();
    }

    public void addPlatform(Platform platform) {
        addPlatform(platform, null);
    }

    // landingUrl은 해당 플랫폼의 작품 페이지 외부 링크 (미확보 시 null)
    public void addPlatform(Platform platform, String landingUrl) {
        this.platforms.add(new WorksPlatform(this, platform, landingUrl));
    }

    // 플랫폼에서 표지 · 연령가 등이 바뀌므로 새 값이 있으면 덮어쓰고, 비어 있으면 기존 값을 둔다
    public List<String> updateFromImport(String author, String illustrator, String originalAuthor,
                                         AgeClassification ageClassification, Genre genre, WorksType worksType,
                                         String description, String thumbnailUrl) {
        List<String> changes = new ArrayList<>();
        this.author = merge("author", author, this.author, changes, true);
        this.illustrator = merge("illustrator", illustrator, this.illustrator, changes, true);
        this.originalAuthor = merge("originalAuthor", originalAuthor, this.originalAuthor, changes, true);
        // 연령은 올리기만. 플랫폼마다 표기가 달라 낮은 값으로 덮이지 않게
        this.ageClassification = merge("ageClassification", AgeClassification.higher(this.ageClassification, ageClassification), this.ageClassification, changes, true);
        this.genre = merge("genre", genre, this.genre, changes, true);
        this.worksType = merge("worksType", worksType, this.worksType, changes, true);
        this.description = merge("description", description, this.description, changes, false);
        this.thumbnailUrl = merge("thumbnailUrl", thumbnailUrl, this.thumbnailUrl, changes, true);
        return changes;
    }

    private static <T> T merge(String field, T incoming, T current, List<String> changes, boolean withValue) {
        if (incoming == null || (incoming instanceof String text && text.isBlank()) || incoming.equals(current)) {
            return current;
        }
        changes.add(withValue ? field + ":" + current + "->" + incoming : field);
        return incoming;
    }

    // 새로 붙으면 platform:+이름(링크), 링크만 바뀌면 landingUrl:이름:이전->새링크, 그대로면 null
    public String putPlatform(Platform platform, String landingUrl) {
        WorksPlatform existing = platforms.stream()
                .filter(worksPlatform -> worksPlatform.getPlatform() == platform)
                .findFirst()
                .orElse(null);
        if (existing == null) {
            String url = hasText(landingUrl) ? landingUrl : null;
            addPlatform(platform, url);
            return "platform:+" + platform.name() + (url == null ? "" : "(" + url + ")");
        }
        String before = existing.getLandingUrl();
        return existing.updateLandingUrl(landingUrl) ? "landingUrl:" + platform.name() + ":" + before + "->" + landingUrl : null;
    }

    // 해시태그는 더하기만. 새로 붙은 태그를 + 로 돌려주고 없으면 null
    public String addHashtags(Set<Hashtag> newHashtags) {
        Set<String> before = hashtags.stream().map(Hashtag::getName).collect(Collectors.toSet());
        List<Hashtag> added = newHashtags.stream()
                .filter(hashtag -> !before.contains(hashtag.getName()))
                .sorted(Comparator.comparing(Hashtag::getName))
                .toList();
        if (added.isEmpty()) return null;
        hashtags.addAll(added);
        return "hashtags:" + added.stream().map(hashtag -> "+" + hashtag.getName()).collect(Collectors.joining(","));
    }

    // 병합: keep 값을 우선하고 비어 있는 필드 · 없는 플랫폼 링크만 drop 값으로 채움
    public void mergeFrom(Works drop) {
        if (!hasText(artistName)) this.artistName = drop.artistName;
        if (!hasText(author)) this.author = drop.author;
        if (!hasText(illustrator)) this.illustrator = drop.illustrator;
        if (!hasText(originalAuthor)) this.originalAuthor = drop.originalAuthor;
        if (!hasText(description)) this.description = drop.description;
        if (!hasText(thumbnailUrl)) this.thumbnailUrl = drop.thumbnailUrl;
        this.ageClassification = AgeClassification.higher(ageClassification, drop.ageClassification);
        if (genre == null) this.genre = drop.genre;
        if (worksType == null) this.worksType = drop.worksType;
        if (Boolean.TRUE.equals(drop.isOnboarding)) this.isOnboarding = true;

        drop.platforms.forEach(dropPlatform -> platforms.stream()
                .filter(platform -> platform.getPlatform() == dropPlatform.getPlatform())
                .findFirst()
                .ifPresentOrElse(
                        platform -> {
                            if (!hasText(platform.getLandingUrl())) platform.updateLandingUrl(dropPlatform.getLandingUrl());
                        },
                        () -> addPlatform(dropPlatform.getPlatform(), dropPlatform.getLandingUrl())));
        hashtags.addAll(drop.hashtags);
    }

    public void applyReviewStats(int reviewsCount, double avgRating) {
        this.reviewsCount = reviewsCount;
        this.avgRating = avgRating;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public Set<Hashtag> getHashtags() {
        return Set.copyOf(hashtags);
    }

    public Set<WorksPlatform> getPlatforms() {
        return Set.copyOf(platforms);
    }
}
