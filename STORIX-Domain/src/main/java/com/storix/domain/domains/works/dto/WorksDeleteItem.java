package com.storix.domain.domains.works.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public record WorksDeleteItem(
        Long worksId,
        Status status,
        String worksName,
        boolean onboarding,
        Map<String, Long> deleted,
        Map<String, Long> blockedBy
) {

    public enum Status { DELETED, DELETABLE, BLOCKED, NOT_FOUND }

    // 리뷰 · 피드 글 · 토픽룸 · 관심작품 · 장르 점수 기록은 사용자 콘텐츠 · 활동 기록이라 같이 지우지 않고 막음
    public static WorksDeleteItem of(Long worksId, String worksName, Boolean onboarding, boolean dryRun,
                                     Map<String, Long> deleted, Map<String, Long> userContents) {
        Map<String, Long> blockedBy = new LinkedHashMap<>();
        userContents.forEach((table, count) -> {
            if (count > 0) blockedBy.put(table, count);
        });
        Status status = !blockedBy.isEmpty() ? Status.BLOCKED : dryRun ? Status.DELETABLE : Status.DELETED;
        return new WorksDeleteItem(worksId, status, worksName, Boolean.TRUE.equals(onboarding), deleted, blockedBy);
    }

    public static WorksDeleteItem notFound(Long worksId) {
        return new WorksDeleteItem(worksId, Status.NOT_FOUND, null, false, Map.of(), Map.of());
    }
}
