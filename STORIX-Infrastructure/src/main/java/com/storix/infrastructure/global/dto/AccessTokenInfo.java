package com.storix.infrastructure.global.dto;

import lombok.Builder;

@Builder
public record AccessTokenInfo (
    Long userId,
    String role
) {
}

