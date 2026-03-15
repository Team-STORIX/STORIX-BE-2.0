package com.storix.storix_api.domains.profile.controller;

import com.storix.storix_api.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.storix_api.domains.profile.application.usecase.ProfileActivityUseCase;
import com.storix.storix_api.domains.profile.application.usecase.ProfileFavoriteUseCase;
import com.storix.storix_api.domains.profile.application.usecase.ProfileUseCase;
import com.storix.storix_api.domains.profile.dto.*;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.user.dto.FavoriteArtistInfo;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "프로필", description = "프로필 관련 API")
public class ProfileController {

    private final ProfileUseCase profileUseCase;
    private final ProfileFavoriteUseCase profileFavoriteUseCase;
    private final ProfileActivityUseCase profileActivityUseCase;

    @Operation(summary = "기본 프로필 조회", description = "기본 프로필을 조회하는 api 입니다.")
    @GetMapping("/me")
    public ResponseEntity<CustomResponse<UserInfo>> getProfile(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return ResponseEntity.ok()
                .body(profileUseCase.getUserProfile(authUserDetails));
    }

    @Operation(summary = "[독자] 닉네임 수정", description = "닉네임을 수정하는 api 입니다.")
    @PostMapping("/reader/nickname")
    public ResponseEntity<CustomResponse<String>> updateNickName(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody UpdateNicknameRequest req
    ) {
        return ResponseEntity.ok()
                .body(profileUseCase.changeNickName(req.nickName(), authUserDetails.getUserId()));
    }

    @Operation(summary = "[독자] 닉네임 중복 체크", description = "닉네임 중복 여부를 체크하는 api 입니다.")
    @GetMapping("/reader/nickname/valid")
    public ResponseEntity<CustomResponse<Void>> nickNameCheck(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam("nickname")
            @NotBlank(message = "닉네임은 필수입니다.")
            @Size(min = 2, max = 10, message = "닉네임은 2~10자까지 가능합니다.")
            @Pattern(
                    regexp = "^[가-힣a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ ]+$",
                    message = "닉네임은 한글, 영문, 숫자, 공백만 가능하며 자음/모음/공백만으로는 불가능합니다."
            )
            String nickName
    ) {
        return ResponseEntity.ok()
                .body(profileUseCase.checkAvailableNickname(nickName, authUserDetails.getUserId()));
    }

    @Operation(summary = "한 줄 소개 변경", description = "한 줄 소개를 변경하는 api 입니다.")
    @PostMapping("/description")
    public ResponseEntity<CustomResponse<String>> updateDescription(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody UpdateDescriptionRequest req
    ) {
        return ResponseEntity.ok()
                .body(profileUseCase.changeDescription(req.profileDescription(), authUserDetails.getUserId()));
    }

    @Operation(summary = "프로필 사진 변경", description = "프로필 사진을 변경하는 api 입니다.   \n이미지를 선택한 직후의 렌더링은 프론트에서 진행해주시고, 이미지를 S3 버킷에 업로드한 후 objectKey와 함께 호출해주세요.")
    @PostMapping("/image")
    public ResponseEntity<CustomResponse<String>> updateImage(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody UpdateImageRequest req
    ) {
        return ResponseEntity.ok()
                .body(profileUseCase.changeImage(req.objectKey(), authUserDetails.getUserId()));
    }

    // 관심 작가 조회
    @Operation(summary = "[독자] 관심 작가 리스트 조회", description = "프로필 관심 작가 리스트를 조회하는 api 입니다. 무한스크롤 형식입니다.")
    @GetMapping("/reader/favorite/artist")
    public ResponseEntity<CustomResponse<ProfileFavoriteArtistWrapperDto<FavoriteArtistInfo>>> getFavoriteArtistList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(defaultValue = "LATEST") ProfileSortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(profileFavoriteUseCase.getFavoriteArtistList(authUserDetails.getUserId(), pageable));
    }

    // 관심 작품 조회
    @Operation(summary = "[독자] 관심 작품 리스트 조회", description = "프로필 관심 작품 리스트를 조회하는 api 입니다. 무한스크롤 형식입니다.")
    @GetMapping("/reader/favorite/works")
    public ResponseEntity<CustomResponse<ProfileFavoriteWorksWrapperDto<FavoriteWorksWithReviewInfo>>> getFavoriteWorksList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(defaultValue = "LATEST") ProfileSortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(profileFavoriteUseCase.getFavoriteWorksList(authUserDetails.getUserId(), pageable));
    }

    // 리뷰 별점 분포 조회
    @Operation(summary = "[독자] 리뷰 별점 분포 조회", description = "프로필 리뷰 별점 분포를 조회하는 api 입니다.")
    @GetMapping("/reader/ratings")
    public ResponseEntity<CustomResponse<RatingCountResponse>> getRatingDistribution(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return ResponseEntity.ok()
                .body(profileFavoriteUseCase.getRatingDistribution(authUserDetails.getUserId()));
    }

    // 선호 해시태그 조회
    @Operation(summary = "[독자] 선호 해시태그 조회", description = "선호 해시태그를 조회하는 api 입니다.")
    @GetMapping("/reader/hashtags")
    public ResponseEntity<CustomResponse<FavoriteHashtagsResponse>> getFavoriteHashtags(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return ResponseEntity.ok()
                .body(profileFavoriteUseCase.getHashtags(authUserDetails.getUserId()));
    }

    // 내가 쓴 게시글 조회
    @Operation(summary = "[독자] 내가 쓴 게시글 조회", description = "프로필 내가 쓴 게시글을 조회하는 api 입니다. 무한스크롤 형식입니다.")
    @GetMapping("/reader/activity/board")
    public ResponseEntity<CustomResponse<Slice<ReaderBoardWithProfileInfo>>> getReaderBoardList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(defaultValue = "LATEST") ProfileSortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(profileActivityUseCase.getReaderBoardList(authUserDetails.getUserId(), pageable));
    }

    // 내가 쓴 댓글 조회
    @Operation(summary = "[독자] 내가 쓴 댓글 조회", description = "프로필 내가 쓴 댓글을 조회하는 api 입니다. 무한스크롤 형식입니다.")
    @GetMapping("/reader/activity/reply")
    public ResponseEntity<CustomResponse<Slice<ReaderBoardReplyInfoWithProfile>>> getReviewList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(defaultValue = "LATEST") ProfileSortType sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(profileActivityUseCase.getReaderBoardReplyList(authUserDetails.getUserId(), pageable));
    }

    // 내가 누른 좋아요 게시글 조회
    @Operation(summary = "[독자] 내가 누른 좋아요 게시글 조회", description = "프로필 내가 누른 좋아요 게시글을 조회하는 api 입니다. 무한스크롤 형식입니다.")
    @GetMapping("/reader/activity/like")
    public ResponseEntity<CustomResponse<Slice<ReaderBoardWithProfileInfo>>> getLikeList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(defaultValue = "LATEST") ProfileSortType sort,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(profileActivityUseCase.getReaderBoardLikeList(authUserDetails.getUserId(), pageable));
    }


}
