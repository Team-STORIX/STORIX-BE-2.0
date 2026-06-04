package com.storix.api.domain.user.controller.dto;

import com.storix.domain.domains.user.domain.TermsType;
import com.storix.domain.domains.user.dto.CreateTermsCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TermsRegisterRequest(

        @NotNull(message = "약관 종류는 필수입니다.")
        TermsType termsType,

        @NotBlank(message = "약관명은 필수입니다.")
        @Size(max = 255, message = "약관명은 255자까지 가능합니다.")
        String title,

        @NotBlank(message = "약관 버전은 필수입니다.")
        @Size(max = 50, message = "약관 버전은 50자까지 가능합니다.")
        String version,

        @NotBlank(message = "약관 원문은 필수입니다.")
        String content,

        @NotNull(message = "필수 약관 여부는 필수입니다.")
        Boolean isRequired,

        // 고지(공지) 일자 - 선택
        LocalDateTime announcedAt,

        @NotNull(message = "시행 시작일은 필수입니다.")
        LocalDateTime effectiveFrom,

        // 적용 종료일 - 선택(null=무기한)
        LocalDateTime effectiveTo

) {
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
