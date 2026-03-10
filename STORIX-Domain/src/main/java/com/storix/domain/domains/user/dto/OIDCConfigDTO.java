package com.storix.domain.domains.user.dto;

public record OIDCConfigDTO(
        String baseUri,
        String clientId
) {
}
