package com.storix.domain.domains.plus.dto;

public record StandardReaderBoardInfo(
        // 유저 정보
        Long userId,

        // 게시글 정보
        Long boardId,
        String content,
        int likeCount,
        int replyCount,
        boolean isSpoiler,

        // 인기 점수
        int popularityScore
) {
}
