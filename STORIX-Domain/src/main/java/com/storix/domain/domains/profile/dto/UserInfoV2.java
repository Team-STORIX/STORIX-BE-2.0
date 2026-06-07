package com.storix.domain.domains.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "프로필 조회 V2 응답. 칭호 및 다음 칭호까지의 진행도 포함 (null 필드도 그대로 응답에 포함)")
@Builder
public record UserInfoV2(

        @Schema(description = "유저 ID", example = "1")
        Long userId,

        @Schema(description = "권한", example = "READER")
        String role,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.storix.com/profile/xxx.png")
        String profileImageUrl,

        @Schema(description = "닉네임", example = "스토릭스독자")
        String nickName,

        @Schema(description = "포인트", example = "120")
        Integer point,

        @Schema(description = "한 줄 소개", example = "로맨스 정주행 중")
        String profileDescription,

        @Schema(description = "소셜 로그인 제공자", example = "kakao")
        String oauthProvider,

        @Schema(description = "대표 장르 (활동 점수가 가장 높은 장르). 점수가 없으면 null.", example = "로맨스")
        String topGenre,

        @Schema(description = "현재 칭호명. 미진입(0~9점)이면 null.", example = "두근거림 수집가")
        String title,

        @Schema(description = "현재 단계 라벨 (미진입/입문/탐색/몰입)", example = "탐색")
        String stage,

        @Schema(description = "다음 단계 라벨. 최고 단계(몰입)면 null.", example = "몰입")
        String nextStage,

        @Schema(description = "대표 장르 점수 (raw_score)", example = "16")
        long topGenreScore,

        @Schema(description = "다음 칭호까지 남은 점수. 최고 단계면 null.", example = "24")
        Integer remainingScore,

        @Schema(description = "현재 단계 진행률 (0~100). 최고 단계면 100.", example = "20.0")
        double progressPercentage
) {
}
