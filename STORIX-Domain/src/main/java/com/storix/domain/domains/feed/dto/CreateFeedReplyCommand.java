package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.feed.domain.ReaderBoardReply;
import com.storix.domain.domains.plus.domain.ReaderBoard;

public record CreateFeedReplyCommand(
        ReaderBoard readerBoard,
        Long userId,
        String comment
) {
    public ReaderBoardReply toEntity() {
        return new ReaderBoardReply(
                readerBoard,
                userId,
                comment
        );
    }
}