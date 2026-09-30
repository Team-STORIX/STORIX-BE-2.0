package com.storix.domain.domains.feed.dto;

public record BookmarkToggleResponse(
        boolean isBookmarked,
        int bookmarkCount
) {}
