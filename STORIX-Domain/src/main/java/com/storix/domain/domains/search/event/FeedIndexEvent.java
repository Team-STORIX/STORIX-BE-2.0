package com.storix.domain.domains.search.event;

public record FeedIndexEvent(
        Long boardId,
        boolean deleted
) {
}
