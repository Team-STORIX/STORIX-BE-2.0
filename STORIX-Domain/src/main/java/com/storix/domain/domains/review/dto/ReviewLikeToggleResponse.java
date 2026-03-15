package com.storix.domain.domains.review.dto;

public record ReviewLikeToggleResponse(
        boolean isLiked,
        int likeCount
) {}
