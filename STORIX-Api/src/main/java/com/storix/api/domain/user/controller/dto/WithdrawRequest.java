package com.storix.api.domain.user.controller.dto;

import com.storix.domain.domains.user.domain.WithdrawReason;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record WithdrawRequest(
        @NotEmpty(message = "탈퇴 사유를 1개 이상 선택해야 합니다.")
        Set<WithdrawReason> reasons,

        // reasons 에 OTHER 가 포함된 경우에만 사용 (100자 이내)
        @Size(max = 100, message = "직접 입력 사유는 100자 이내여야 합니다.")
        String detail
) {
    @AssertTrue(message = "OTHER 사유 선택 시 detail 은 필수이며, 그 외 사유에서는 detail 을 보낼 수 없습니다.")
    private boolean isDetailConsistent() {
        boolean otherSelected = reasons != null && reasons.contains(WithdrawReason.OTHER);
        boolean detailPresent = detail != null && !detail.isBlank();
        return otherSelected == detailPresent;
    }
}
