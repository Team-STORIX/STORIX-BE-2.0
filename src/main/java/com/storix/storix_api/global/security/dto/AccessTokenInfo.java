package com.storix.storix_api.global.security.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
public record AccessTokenInfo (
    Long userId,
    String role
) {
}

