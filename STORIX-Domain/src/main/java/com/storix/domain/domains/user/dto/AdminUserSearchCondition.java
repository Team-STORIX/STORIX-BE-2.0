package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.AccountState;

public record AdminUserSearchCondition(
        Long userId,
        String nickName,
        AccountState accountState
) {
}
