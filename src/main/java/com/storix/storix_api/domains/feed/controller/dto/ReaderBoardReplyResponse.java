package com.storix.storix_api.domains.feed.controller.dto;

import com.storix.storix_api.domains.feed.dto.StandardReplyInfo;
import com.storix.storix_api.domains.user.dto.StandardProfileInfo;

public record ReaderBoardReplyResponse(
        StandardProfileInfo profile,
        StandardReplyInfo content
) {
    public static ReaderBoardReplyResponse of(StandardProfileInfo profile, StandardReplyInfo content) {
        return new ReaderBoardReplyResponse(
                profile,
                content
        );
    }
}
