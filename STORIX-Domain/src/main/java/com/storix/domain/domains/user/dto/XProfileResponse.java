package com.storix.domain.domains.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record XProfileResponse(
        XUserResponse data // { "data": { "id", "name", "username" } }
) {
}
