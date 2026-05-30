package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.UserBlock;

public record BlockUserCommand(
        Long blockerId,
        Long blockedUserId
) {
    public UserBlock toEntity() {
        return new UserBlock(blockerId, blockedUserId);
    }
}
