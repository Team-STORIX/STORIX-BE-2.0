
package com.storix.storix_api.domains.feed.dto;

public record LikeToggleResponse(
        boolean isLiked,
        int likeCount
) {}
