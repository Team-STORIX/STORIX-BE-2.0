package com.storix.storix_api.domains.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {

    NEW_FOLLOWER,
    NEW_LIKE,
    NEW_COMMENT,
    SYSTEM
}
