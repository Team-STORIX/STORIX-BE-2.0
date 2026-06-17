package com.storix.domain.domains.user.event;

import java.time.LocalDateTime;

public record UserAccessRevokedEvent(
        Long userId,
        UserAccessRevokedType type,
        LocalDateTime suspendedUntil
) {

    public static UserAccessRevokedEvent suspended(Long userId, LocalDateTime suspendedUntil) {
        return new UserAccessRevokedEvent(userId, UserAccessRevokedType.SUSPENDED, suspendedUntil);
    }

    public static UserAccessRevokedEvent withdrawn(Long userId) {
        return new UserAccessRevokedEvent(userId, UserAccessRevokedType.WITHDRAWN, null);
    }

    public enum UserAccessRevokedType {
        SUSPENDED, WITHDRAWN
    }
}
