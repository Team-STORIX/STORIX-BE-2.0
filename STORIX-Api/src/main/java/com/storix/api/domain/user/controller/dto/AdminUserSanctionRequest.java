package com.storix.api.domain.user.controller.dto;

import com.storix.domain.domains.report.domain.TargetContentType;
import com.storix.domain.domains.user.domain.UserSanctionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminUserSanctionRequest(
        @Schema(description = "제재 타입. CONTENT_DELETED 선택 시 targetType과 targetId가 필수입니다.", example = "CONTENT_DELETED")
        @NotNull UserSanctionType type,
        @Schema(description = "삭제 대상 콘텐츠 타입. CONTENT_DELETED 처리 시 필수입니다. "
                + "신고(report)와 동일한 TargetContentType enum을 사용하며, 콘텐츠 삭제에는 아래 값만 활용해주세요. "
                + "FEED=게시글, FEED_REPLY=댓글, REVIEW=리뷰, CHAT=채팅 메시지 "
                + "(TOPIC_ROOM은 유저 신고 전용이라 콘텐츠 삭제 대상이 아님)", example = "FEED")
        TargetContentType targetType,
        @Schema(description = "삭제 대상 콘텐츠 ID. CONTENT_DELETED 처리 시 필수입니다. CHAT은 chatMessageId를 의미합니다.", example = "1")
        @Positive Long targetId,
        @Schema(description = "관리자 제재 메모", example = "관리자 수동 콘텐츠 삭제")
        @Size(max = 500) String memo
) {

    @AssertTrue(message = "CONTENT_DELETED 처리 시 targetType과 targetId는 필수입니다")
    @Schema(hidden = true)
    public boolean isValidContentDeleteTarget() {
        return type != UserSanctionType.CONTENT_DELETED || (targetType != null && targetId != null);
    }
}
