package com.storix.api.domain.user.controller.dto;

import com.storix.domain.domains.user.domain.Terms;
import com.storix.domain.domains.user.domain.TermsType;

public record TermsRegisterResponse(
        Long id,
        TermsType termsType,
        String title,
        String version
) {
    public static TermsRegisterResponse from(Terms terms) {
        return new TermsRegisterResponse(
                terms.getId(),
                terms.getTermsType(),
                terms.getTitle(),
                terms.getVersion()
        );
    }
}
