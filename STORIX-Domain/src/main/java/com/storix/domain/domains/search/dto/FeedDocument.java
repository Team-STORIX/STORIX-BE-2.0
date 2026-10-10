package com.storix.domain.domains.search.dto;

import com.storix.domain.domains.plus.domain.ReaderBoard;

import java.time.ZoneId;
import java.util.List;

public record FeedDocument(
        Long boardId,
        Long userId,
        String content,
        String worksName,
        List<String> nicknames,
        long createdAt
) {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static FeedDocument of(ReaderBoard board, String worksName, List<String> nicknames) {
        return new FeedDocument(
                board.getId(),
                board.getUserId(),
                board.getContent(),
                worksName,
                nicknames,
                board.getCreatedAt().atZone(ZONE).toInstant().toEpochMilli()
        );
    }
}
