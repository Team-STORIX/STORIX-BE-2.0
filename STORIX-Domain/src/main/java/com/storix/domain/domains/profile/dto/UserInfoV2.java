package com.storix.domain.domains.profile.dto;

import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.user.domain.TitleStage;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.domain.Genre;
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
        double progressPercentage,

        @Schema(description = "서재 공개 여부", example = "true")
        boolean isLibraryPublic
) {
    public static UserInfoV2 of(User user, long topGenreScore, String baseUrl) {
        Title title = user.getTitle();
        Genre topGenre = title == null ? null : title.getGenre();
        TitleStage stage = title == null ? TitleStage.NONE : title.getStage();

        return UserInfoV2.builder()
                .userId(user.getId())
                .role(user.getRole().toString())
                .nickName(user.getDisplayNickName())
                .profileDescription(user.getProfileDescription())
                .profileImageUrl(user.getProfileObjectKey() == null
                        ? null : baseUrl + "/" + user.getProfileObjectKey())
                .oauthProvider(user.getOauthInfo() == null
                        ? null : user.getOauthInfo().getProvider().getDbValue())
                .topGenre(topGenre == null ? null : topGenre.getDbValue())
                .title(title == null ? null : title.getDisplayName())
                .stage(stage.getLabel())
                .nextStage(title == null ? null : stage.next().map(TitleStage::getLabel).orElse(null))
                .topGenreScore(topGenreScore)
                .remainingScore(title == null || stage.isMax() ? null : stage.getNextScore() - (int) topGenreScore)
                .progressPercentage(title == null ? 0.0 : stage.progressPercentage(topGenreScore))
                .isLibraryPublic(user.isLibraryPublic())
                .build();
    }
}
