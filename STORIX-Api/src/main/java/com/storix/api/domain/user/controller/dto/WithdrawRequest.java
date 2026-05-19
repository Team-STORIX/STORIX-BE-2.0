package com.storix.api.domain.user.controller.dto;

import com.storix.domain.domains.user.domain.WithdrawReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WithdrawRequest(
        @NotNull(message = "탈퇴 사유는 필수입니다.")
        WithdrawReason reason,

        // reason == OTHER 일 때만 사용 (200자 이내)
        @Size(max = 200, message = "직접 입력 사유는 200자 이내여야 합니다.")
        String detail
) {
}
