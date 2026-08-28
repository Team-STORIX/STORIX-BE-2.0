package com.storix.domain.domains.adultverification.dto;

import java.time.LocalDateTime;

// 유저별 최신 인증 시각 일괄 조회용
public record LatestVerifiedAt(
        Long userId,
        LocalDateTime verifiedAt
) {
}
