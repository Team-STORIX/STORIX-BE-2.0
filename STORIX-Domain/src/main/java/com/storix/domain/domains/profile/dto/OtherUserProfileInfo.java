package com.storix.domain.domains.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "타 사용자 프로필 조회 응답. 포인트·소셜 로그인 제공자는 내리지 않는다")
@Builder
public record OtherUserProfileInfo(

        @Schema(description = "유저 ID", example = "1")
        Long userId,

        @Schema(description = "권한", example = "READER")
        String role,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.storix.com/profile/xxx.png")
        String profileImageUrl,

        @Schema(description = "닉네임", example = "스토릭스독자")
        String nickName,

        @Schema(description = "한 줄 소개", example = "로맨스 정주행 중")
        String profileDescription,

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
        double progressPercentage,

        @Schema(description = "서재 공개 여부. false 면 서재 목록이 내려가지 않는다", example = "true")
        boolean isLibraryPublic
) {
    public static OtherUserProfileInfo from(UserInfoV2 info) {
        return OtherUserProfileInfo.builder()
                .userId(info.userId())
                .role(info.role())
                .profileImageUrl(info.profileImageUrl())
                .nickName(info.nickName())
                .profileDescription(info.profileDescription())
                .topGenre(info.topGenre())
                .title(info.title())
                .stage(info.stage())
                .nextStage(info.nextStage())
                .topGenreScore(info.topGenreScore())
                .remainingScore(info.remainingScore())
                .progressPercentage(info.progressPercentage())
                .isLibraryPublic(info.isLibraryPublic())
                .build();
    }
}
