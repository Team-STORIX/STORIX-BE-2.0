package com.storix.domain.domains.user.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "terms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_terms_type_version", columnNames = {"terms_type", "version"})
        },
        indexes = {
                @Index(name = "idx_terms_type_effective", columnList = "terms_type, effective_from")
        }
)
public class Terms extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 약관 종류
    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 50)
    private TermsType termsType;

    // 약관 명
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // 약관 버전
    @Column(name = "version", nullable = false, length = 50)
    private String version;

    // 약관 원문
    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;

    // 필수 약관 여부
    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    // 고지 일자
    @Column(name = "announced_at")
    private LocalDate announcedAt;

    // 시행 시작일
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    // 적용 종료일
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Builder
    private Terms(TermsType termsType, String title, String version, String content,
                  boolean isRequired, LocalDate announcedAt,
                  LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.termsType = termsType;
        this.title = title;
        this.version = version;
        this.content = content;
        this.isRequired = isRequired;
        this.announcedAt = announcedAt;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }
}
