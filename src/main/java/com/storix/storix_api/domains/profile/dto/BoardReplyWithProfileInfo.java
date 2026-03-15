package com.storix.storix_api.domains.profile.dto;

public record BoardReplyWithProfileInfo(
        // 리뷰 작성 유저
        Long userId,
        String nickName,
        String profileImageUrl

        // 댓글 정보

) {
}
