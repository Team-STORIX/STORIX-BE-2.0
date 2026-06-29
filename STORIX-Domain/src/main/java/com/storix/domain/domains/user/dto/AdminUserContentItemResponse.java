package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.plus.domain.Rating;

import java.time.LocalDateTime;

public record AdminUserContentItemResponse(
        Long contentId,
        AdminUserContentType type,
        Long boardId,
        Long parentReplyId,
        Long roomId,
        Long worksId,
        String content,
        Rating rating,
        MessageType messageType,
        int likeCount,
        int replyCount,
        LocalDateTime createdAt
) {
}
