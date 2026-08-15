package com.storix.domain.domains.topicroom.dto;

public record RecentSender(
        Long userId,
        String nickname,
        String profileImageUrl
) {
}
