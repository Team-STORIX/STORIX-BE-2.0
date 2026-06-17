package com.storix.api.domain.user.controller.dto;

import com.storix.domain.domains.user.domain.TermsType;
import com.storix.domain.domains.user.dto.CreateTermsCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TermsRegisterRequest(

        @Schema(description = "약관 종류", example = "SERVICE")
        @NotNull(message = "약관 종류는 필수입니다.")
        TermsType termsType,

        @Schema(description = "약관명", example = "서비스 이용약관")
        @NotBlank(message = "약관명은 필수입니다.")
        @Size(max = 255, message = "약관명은 255자까지 가능합니다.")
        String title,

        @Schema(description = "약관 버전", example = "1.0")
        @NotBlank(message = "약관 버전은 필수입니다.")
        @Size(max = 50, message = "약관 버전은 50자까지 가능합니다.")
        String version,

        @Schema(description = "약관 원문", example = "제1조(목적) 본 약관은 ...")
        @NotBlank(message = "약관 원문은 필수입니다.")
        String content,

        @Schema(description = "필수 약관 여부", example = "true")
        @NotNull(message = "필수 약관 여부는 필수입니다.")
        Boolean isRequired,

        @Schema(description = "고지 일자", example = "2026-06-04", format = "date")
        @NotNull(message = "고지 일자는 필수입니다.")
        LocalDate announcedAt,

        @Schema(description = "시행 시작일", example = "2026-06-04", format = "date")
        @NotNull(message = "시행 시작일은 필수입니다.")
        LocalDate effectiveFrom,

        @Schema(description = "적용 종료일 (null=무기한)", example = "2026-12-31", format = "date")
        LocalDate effectiveTo

) {
    // 날짜 범위 검증: 고지일 <= 시행일, 종료일(있다면) >= 시행일
    @AssertTrue(message = "약관 날짜 범위가 올바르지 않습니다.")
    private boolean isDateRangeValid() {
        if (announcedAt == null || effectiveFrom == null) return true;
        if (announcedAt.isAfter(effectiveFrom)) return false;
        return effectiveTo == null || !effectiveTo.isBefore(effectiveFrom);
    }

    public CreateTermsCommand toCommand() {
        return new CreateTermsCommand(
                termsType,
                title,
                version,
                content,
                isRequired,
                announcedAt,
                effectiveFrom,
                effectiveTo
        );
    }
}
