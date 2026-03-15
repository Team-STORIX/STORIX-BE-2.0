package com.storix.storix_api.domains.user.dto;

public record OIDCConfigDTO(
        String baseUri,
        String clientId
) {
}
