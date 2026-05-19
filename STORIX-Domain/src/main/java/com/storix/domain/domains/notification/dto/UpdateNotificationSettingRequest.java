package com.storix.domain.domains.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 추후 알림 뎁스 세분화를 위한 테이블 컬럼 유지 (9개 -> 4개 상위 분류)
public record UpdateNotificationSettingRequest(

        @Schema(description = "내 활동 알림 — 피드/리뷰/댓글/답댓글/댓글 좋아요", example = "true")
        Boolean myActivityEnabled,

        @Schema(description = "콘텐츠/커뮤니티 알림 — 오늘의 피드/HOT 토픽룸 선정", example = "true")
        Boolean contentCommunityEnabled,

        @Schema(description = "이벤트/혜택 알림 — 운영자 발송 이벤트/광고", example = "false")
        Boolean eventBenefitEnabled,

        @Schema(description = "운영/정책 알림 — 신고 처리/이용 제한/약관 업데이트 등", example = "true")
        Boolean operationPolicyEnabled
) {
    public UpdateNotificationSettingCommand toCommand() {
        return new UpdateNotificationSettingCommand(
                myActivityEnabled,
                myActivityEnabled,
                myActivityEnabled,
                myActivityEnabled,
                myActivityEnabled,
                contentCommunityEnabled,
                contentCommunityEnabled,
                eventBenefitEnabled,
                operationPolicyEnabled
        );
    }
}
