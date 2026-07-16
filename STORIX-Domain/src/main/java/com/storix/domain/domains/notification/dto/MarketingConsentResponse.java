package com.storix.domain.domains.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

// 마케팅 동의/거부 처리 결과 (모달 표시용)
public record MarketingConsentResponse(
        String title,         // 이벤트/혜택 알림 동의/거부 안내
        String sender,        // 팀 스토릭스 (응답 표기명 — 내부에서는 processor 로 관리)
        @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
        LocalDateTime processedAt,
        String description    // 알림 동의/거부 처리 완료
) {
    public static MarketingConsentResponse of(MarketingConsentResult result, String title, String description) {
        return new MarketingConsentResponse(title, result.processor(), result.processedAt(), description);
    }
}
