package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AttendanceDrawWinner(

        @Schema(description = "뽑힌 순서 (1부터). 상품이 나뉘는 이벤트는 이 순서로 앞에서 N명씩 끊어 씁니다.", example = "1")
        int drawOrder,

        @Schema(description = "유저 ID", example = "42")
        Long userId,

        // 당첨 안내는 푸시 알림 + 구글 폼으로 진행
        @Schema(description = "닉네임 (탈퇴 시 마스킹된 표시명)")
        String nickName,

        @Schema(description = "보유 응모권 수 (추첨 가중치)", example = "5")
        int ticketCount,

        @Schema(description = "누적 출석일 수", example = "12")
        int totalAttendedDays
) {
}
