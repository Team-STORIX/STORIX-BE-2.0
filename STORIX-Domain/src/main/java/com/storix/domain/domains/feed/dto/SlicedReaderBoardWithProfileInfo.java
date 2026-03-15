package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.plus.dto.ReaderBoardInfo;
import com.storix.domain.domains.user.dto.StandardProfileInfo;

public record SlicedReaderBoardWithProfileInfo(
        // 게시글 작성 유저
        StandardProfileInfo profile,

        // 게시글 정보
        ReaderBoardInfo board
) {
    public static SlicedReaderBoardWithProfileInfo of(
            StandardProfileInfo profile,
            ReaderBoardInfo board
    ) {
        return new SlicedReaderBoardWithProfileInfo(
                profile,
                board
        );
    }
}
