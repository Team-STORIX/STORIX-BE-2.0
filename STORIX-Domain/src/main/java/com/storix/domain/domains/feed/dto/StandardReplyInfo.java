package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.feed.domain.ReaderBoardReply;

public record StandardReplyInfo(
        // 댓글 정보
        Long replyId,
        String content,
        int likeCount,
        int depth,
        int childReplyCount,
        Long parentReplyId,
        boolean deleted
) {
    public static StandardReplyInfo from(
            ReaderBoardReply reply
    ) {
        return new StandardReplyInfo(
                reply.getId(),
                reply.getDisplayComment(),
                reply.getLikeCount(),
                reply.getDepth(),
                reply.getChildReplyCount(),
                reply.getParentReply() != null ? reply.getParentReply().getId() : null,
                reply.isDeleted()
        );
    }
}
