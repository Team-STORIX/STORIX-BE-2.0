package com.storix.storix_api.domains.feed.dto;

import com.storix.storix_api.domains.feed.domain.ReaderBoardReply;
import com.storix.storix_api.domains.plus.domain.ReaderBoard;

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