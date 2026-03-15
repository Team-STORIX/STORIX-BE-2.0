package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.user.dto.StandardProfileInfo;

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
