package com.storix.api.domain.profile.controller;

import com.storix.api.domain.profile.usecase.OtherProfileUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.library.domain.LibrarySortType;
import com.storix.domain.domains.preference.dto.GenreScoreInfo;
import com.storix.domain.domains.profile.dto.FavoriteHashtagsResponse;
import com.storix.domain.domains.profile.dto.OtherUserFavoriteWorksInfo;
import com.storix.domain.domains.profile.dto.OtherUserLibraryResponse;
import com.storix.domain.domains.profile.dto.OtherUserProfileInfo;
import com.storix.domain.domains.profile.dto.ProfileFavoriteWorksWrapperDto;
import com.storix.domain.domains.profile.dto.ProfileSortType;
import com.storix.domain.domains.profile.dto.RatingCountResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/profile/other-users/{userId}")
@RequiredArgsConstructor
@Tag(name = "타 사용자 프로필", description = "타 사용자 프로필 관련 API")
public class OtherProfileController {

    private final OtherProfileUseCase otherProfileUseCase;

    @Operation(summary = "타 사용자 프로필 조회", description = "타 사용자의 프로필을 조회하는 api 입니다. 탈퇴·정지 유저는 404, 내가 차단한 유저는 403, 본인 id 는 400 입니다. 본인 프로필은 /api/v2/profile/me 를 사용해주세요.")
    @GetMapping
    public ResponseEntity<CustomResponse<OtherUserProfileInfo>> getProfile(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok()
                .body(otherProfileUseCase.getProfile(authUserDetails.getUserId(), userId));
    }

    @Operation(summary = "타 사용자 관심 작품 리스트 조회", description = "타 사용자의 관심 작품 리스트를 조회하는 api 입니다. 무한스크롤 형식입니다. 평가 여부와 별점은 내려가지 않고, 성인 작품은 조회하는 유저의 인증 여부로 가려집니다.")
    @GetMapping("/favorite/works")
    public ResponseEntity<CustomResponse<ProfileFavoriteWorksWrapperDto<OtherUserFavoriteWorksInfo>>> getFavoriteWorksList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "LATEST") ProfileSortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(otherProfileUseCase.getFavoriteWorksList(authUserDetails.getUserId(), userId, pageable));
    }

    @Operation(summary = "타 사용자 리뷰 별점 분포 조회", description = "타 사용자의 리뷰 별점 분포를 조회하는 api 입니다.")
    @GetMapping("/ratings")
    public ResponseEntity<CustomResponse<RatingCountResponse>> getRatingDistribution(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok()
                .body(otherProfileUseCase.getRatingDistribution(authUserDetails.getUserId(), userId));
    }

    @Operation(summary = "타 사용자 선호 장르 통계 조회", description = "타 사용자의 선호 장르별 점수를 조회하는 api 입니다.")
    @GetMapping("/stats")
    public ResponseEntity<CustomResponse<List<GenreScoreInfo>>> getStats(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok()
                .body(otherProfileUseCase.getGenreStats(authUserDetails.getUserId(), userId));
    }

    @Operation(summary = "타 사용자 선호 해시태그 조회", description = "타 사용자의 선호 해시태그를 조회하는 api 입니다.")
    @GetMapping("/hashtags")
    public ResponseEntity<CustomResponse<FavoriteHashtagsResponse>> getFavoriteHashtags(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok()
                .body(otherProfileUseCase.getHashtags(authUserDetails.getUserId(), userId));
    }

    @Operation(summary = "타 사용자 서재 조회", description = "타 사용자가 리뷰한 작품 정보와 평점을 조회하는 api 입니다. 무한스크롤 형식입니다. 비공개 서재면 isPublic=false 와 빈 목록이 내려갑니다.")
    @GetMapping("/library")
    public ResponseEntity<CustomResponse<OtherUserLibraryResponse>> getLibrary(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "LATEST") LibrarySortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(otherProfileUseCase.getLibrary(authUserDetails.getUserId(), userId, pageable));
    }
}
