package com.storix.domain.domains.feed.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.domain.domains.user.dto.StandardProfileInfo;

import java.util.List;

public record ReaderBoardReplyInfoWithProfile(
        StandardProfileInfo profile,
        StandardReplyInfoWithLike reply,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<ReaderBoardReplyInfoWithProfile> childReplies
) {
    // 답댓글용 (childReplies 필드 자체를 제외)
    public static ReaderBoardReplyInfoWithProfile of(StandardProfileInfo profile, StandardReplyInfoWithLike reply) {
        return new ReaderBoardReplyInfoWithProfile(profile, reply, null);
    }

    // 최상위 댓글용 (답댓글 리스트 포함)
    public static ReaderBoardReplyInfoWithProfile of(StandardProfileInfo profile, StandardReplyInfoWithLike reply, List<ReaderBoardReplyInfoWithProfile> childReplies) {
        return new ReaderBoardReplyInfoWithProfile(profile, reply, childReplies);
    }
}