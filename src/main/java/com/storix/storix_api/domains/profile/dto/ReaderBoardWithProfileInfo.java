package com.storix.storix_api.domains.profile.dto;

import com.storix.storix_api.domains.feed.dto.BoardWorksInfo;
import com.storix.storix_api.domains.plus.dto.ReaderBoardImageInfo;
import com.storix.storix_api.domains.plus.dto.ReaderBoardInfo;
import com.storix.storix_api.domains.user.dto.StandardProfileInfo;
import com.storix.storix_api.domains.works.dto.WorksInfo;

import java.util.List;

public record ReaderBoardWithProfileInfo(
        // 게시글 작성 유저
        StandardProfileInfo profile,

        // 게시글 정보
        ReaderBoardInfo board,
        List<ReaderBoardImageInfo> images,

        // 게시글 작품 정보
        BoardWorksInfo works
) {
    public static ReaderBoardWithProfileInfo of(
            StandardProfileInfo profile,
            ReaderBoardInfo board,
            List<ReaderBoardImageInfo> images,
            WorksInfo works,
            List<String> hashtags
    ) {
        return new ReaderBoardWithProfileInfo(
                profile,
                board,
                images,
                BoardWorksInfo.from(works, hashtags)
        );
    }
}