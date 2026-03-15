package com.storix.storix_api.domains.review.dto;

public record ReviewLikeToggleResponse(
        boolean isLiked,
        int likeCount
) {}
