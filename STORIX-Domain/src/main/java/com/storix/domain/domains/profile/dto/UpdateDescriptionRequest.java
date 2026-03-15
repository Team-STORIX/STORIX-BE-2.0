package com.storix.domain.domains.profile.dto;

import jakarta.validation.constraints.Size;

public record UpdateDescriptionRequest(
        @Size(max = 30, message = "한 줄 소개는 30자까지 가능합니다.")
        String profileDescription
) {
}
