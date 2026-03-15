
package com.storix.domain.domains.feed.dto;

public record LikeToggleResponse(
        boolean isLiked,
        int likeCount
) {}
