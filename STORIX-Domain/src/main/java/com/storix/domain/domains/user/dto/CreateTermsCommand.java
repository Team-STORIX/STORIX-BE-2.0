package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.Terms;
import com.storix.domain.domains.user.domain.TermsType;

import java.time.LocalDate;

public record CreateTermsCommand(
        TermsType termsType,
        String title,
        String version,
        String content,
        boolean isRequired,
        LocalDate announcedAt,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
    public Terms toEntity() {
        return Terms.builder()
                .termsType(termsType)
                .title(title)
                .version(version)
                .content(content)
                .isRequired(isRequired)
                .announcedAt(announcedAt)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(effectiveTo)
                .build();
    }
}
