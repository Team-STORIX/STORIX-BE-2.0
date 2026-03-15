package com.storix.storix_api.domains.feed.dto;

import com.storix.storix_api.domains.feed.domain.ReaderBoardReply;

public record StandardReplyInfo(
        // 댓글 정보
        Long replyId,
        String content,
        int likeCount
) {
    public static StandardReplyInfo from(
            ReaderBoardReply reply
    ) {
        return new StandardReplyInfo(
                reply.getId(),
                reply.getComment(),
                reply.getLikeCount()
        );
    }
}
