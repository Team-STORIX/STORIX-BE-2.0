package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AttendanceDrawWinner(

        @Schema(description = "당첨 순위 (1위부터)", example = "1")
        int rank,

        Long userId,

        @Schema(description = "닉네임")
        String nickName,

        @Schema(description = "소셜 로그인 이메일 (경품 발송 연락처)")
        String email,

        @Schema(description = "프로필 이미지 URL (미설정 시 null)")
        String profileImageUrl,

        @Schema(description = "보유 응모권 수 (추첨 가중치)", example = "5")
        int ticketCount,

        @Schema(description = "누적 출석일 수", example = "12")
        int totalAttendedDays
) {
}
