package com.storix.domain.domains.user.dto;

import jakarta.validation.constraints.NotBlank;

public record DeveloperLoginRequest(
    @NotBlank(message = "pendingId는 필수입니다.")
    String pendingId
) {
}
