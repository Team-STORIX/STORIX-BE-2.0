package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.user.dto.StandardProfileInfo;

public record ReaderBoardReplyInfoWithProfile(
        StandardProfileInfo profile,
        StandardReplyInfoWithLike reply
) {
    public static ReaderBoardReplyInfoWithProfile of(StandardProfileInfo profile, StandardReplyInfoWithLike reply) {
        return new ReaderBoardReplyInfoWithProfile(profile, reply);
    }
}