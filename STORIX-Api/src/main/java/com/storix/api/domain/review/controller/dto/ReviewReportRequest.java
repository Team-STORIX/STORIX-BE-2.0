package com.storix.api.domain.review.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewReportRequest(
        @NotNull(message = "신고 대상인 사용자의 id 값을 보내주세요.")
        Long reportedUserId,

        @Size(max = 100, message = "기타 신고 사유는 100자까지 입력 가능합니다.")
        String otherReason // 기타
) {
}
