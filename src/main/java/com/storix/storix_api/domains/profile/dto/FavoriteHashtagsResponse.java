package com.storix.storix_api.domains.profile.dto;

import java.util.Map;

public record FavoriteHashtagsResponse(
        Map<Integer, String> rankings
) {
}
