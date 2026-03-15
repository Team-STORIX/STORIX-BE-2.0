package com.storix.storix_api.domains.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record UserInfo(
    Long userId,
    String role,
    String profileImageUrl,
    String nickName,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer level,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer point,

    String profileDescription
) {
}
