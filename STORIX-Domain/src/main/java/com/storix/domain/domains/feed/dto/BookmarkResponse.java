package com.storix.domain.domains.feed.dto;

public record BookmarkResponse(
        boolean isBookmarked,
        int bookmarkCount
) {}
