package com.storix.domain.domains.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

// 이벤트 유형과 무관한 당첨자 공통 표현. 유형 고유 정보(응모권 수 등)는 유형별 응답에서 채운다
@Builder
public record AppEventDrawWinner(

        @Schema(description = "뽑힌 순서 (1부터). 상품이 나뉘는 이벤트는 이 순서로 앞에서 N명씩 끊어 씁니다.", example = "1")
        int drawOrder,

        @Schema(description = "유저 ID", example = "42")
        Long userId,

        // 당첨 안내는 푸시 알림 + 구글 폼으로 진행
        @Schema(description = "닉네임 (탈퇴 시 마스킹된 표시명)")
        String nickName
) {
}
