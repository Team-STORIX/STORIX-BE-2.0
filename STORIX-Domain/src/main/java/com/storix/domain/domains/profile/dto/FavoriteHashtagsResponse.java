package com.storix.domain.domains.profile.dto;

import java.util.Map;

public record FavoriteHashtagsResponse(
        Map<Integer, String> rankings
) {
}
