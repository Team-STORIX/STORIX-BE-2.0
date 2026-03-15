package com.storix.storix_api.domains.feed.dto;

import com.storix.storix_api.domains.user.dto.StandardProfileInfo;

public record ReaderBoardReplyInfoWithProfile(
        StandardProfileInfo profile,
        StandardReplyInfoWithLike reply
) {
    public static ReaderBoardReplyInfoWithProfile of(StandardProfileInfo profile, StandardReplyInfoWithLike reply) {
        return new ReaderBoardReplyInfoWithProfile(profile, reply);
    }
}