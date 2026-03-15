package com.storix.storix_api.domains.home.dto;

import com.storix.storix_api.domains.plus.dto.ReaderBoardInfo;
import com.storix.storix_api.domains.user.dto.StandardProfileInfo;

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
