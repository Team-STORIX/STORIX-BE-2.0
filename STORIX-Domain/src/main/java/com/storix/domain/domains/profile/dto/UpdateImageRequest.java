package com.storix.domain.domains.profile.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateImageRequest(
        @NotNull(message = "업로드한 프로필 이미지의 objectKey를 보내주세요.")
        String objectKey
) {
}