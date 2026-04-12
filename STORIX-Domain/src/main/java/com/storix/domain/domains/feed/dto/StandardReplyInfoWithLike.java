package com.storix.domain.domains.feed.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record StandardReplyInfoWithLike(
        Long replyId,
        Long userId,
        Long boardId,
        String comment,
        String lastCreatedTime,
        int likeCount,
        boolean isLiked,
        int depth,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer childReplyCount,
        Long parentReplyId,
        boolean deleted
) {
    public static StandardReplyInfoWithLike of(ReaderBoardReplyInfo reply, boolean isLiked) {
        return new StandardReplyInfoWithLike(
                reply.replyId(),
                reply.userId(),
                reply.boardId(),
                reply.comment(),
                reply.lastCreatedTime(),
                reply.likeCount(),
                isLiked,
                reply.depth(),
                reply.depth() >= 1 ? null : reply.childReplyCount(),
                reply.parentReplyId(),
                reply.deleted()
        );
    }
}