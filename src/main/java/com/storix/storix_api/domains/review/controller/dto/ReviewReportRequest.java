package com.storix.storix_api.domains.review.controller.dto;

import com.storix.storix_api.domains.topicroom.domain.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewReportRequest(
        @NotNull(message = "신고 대상인 사용자의 id 값을 보내주세요.")
        Long reportedUserId,

        @NotNull(message = "신고 사유를 보내주세요.")
        ReportReason reason,

        @Size(max = 100, message = "기타 신고 사유는 100자까지 입력 가능합니다.")
        String otherReason // 기타
) {
}
