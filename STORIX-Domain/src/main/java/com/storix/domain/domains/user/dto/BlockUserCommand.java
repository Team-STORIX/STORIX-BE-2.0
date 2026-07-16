package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.UserBlock;
import org.springframework.lang.NonNull;

public record BlockUserCommand(
        Long blockerId,
        Long blockedUserId
) {
    @NonNull
    public UserBlock toEntity() {
        return new UserBlock(blockerId, blockedUserId);
    }
}
