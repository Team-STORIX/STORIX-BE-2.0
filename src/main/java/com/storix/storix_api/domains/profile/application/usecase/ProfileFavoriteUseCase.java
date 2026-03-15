package com.storix.storix_api.domains.profile.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.profile.application.service.ProfileFavoriteService;
import com.storix.storix_api.domains.profile.dto.*;
import com.storix.storix_api.domains.user.dto.FavoriteArtistInfo;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class ProfileFavoriteUseCase {

    private final ProfileFavoriteService profileFavoriteService;

    // 관심 작가 조회
    public CustomResponse<ProfileFavoriteArtistWrapperDto<FavoriteArtistInfo>> getFavoriteArtistList(Long userId, Pageable pageable) {

        // 관심 작가 등록수
        int totalFavoriteArtistCount = profileFavoriteService.findTotalFavoriteArtistCount(userId);
        // 관심 작가 정보
        Slice<FavoriteArtistInfo> favoriteArtistInfos = profileFavoriteService.findAllFavoriteArtistInfo(userId, pageable);

        ProfileFavoriteArtistWrapperDto<FavoriteArtistInfo> result
                = new ProfileFavoriteArtistWrapperDto<>(totalFavoriteArtistCount, favoriteArtistInfos);

        return CustomResponse.onSuccess(SuccessCode.PROFILE_FAVORITE_ARTIST_LIST_LOAD_SUCCESS, result);
    }

    // 관심 작품 조회
    public CustomResponse<ProfileFavoriteWorksWrapperDto<FavoriteWorksWithReviewInfo>> getFavoriteWorksList(Long userId, Pageable pageable) {

        // 관심 작품 등록수
        int totalFavoriteWorksCount = profileFavoriteService.findTotalFavoriteWorksCount(userId);
        // 관심 작품 정보 조회
        Slice<FavoriteWorksWithReviewInfo> favoriteWorksInfos = profileFavoriteService.findAllFavoriteWorksInfo(userId, pageable);

        ProfileFavoriteWorksWrapperDto<FavoriteWorksWithReviewInfo> result
                = new ProfileFavoriteWorksWrapperDto<>(totalFavoriteWorksCount, favoriteWorksInfos);

        return CustomResponse.onSuccess(SuccessCode.PROFILE_FAVORITE_WORKS_LIST_LOAD_SUCCESS, result);
    }

    // 리뷰 별점 분포 조회
    public CustomResponse<RatingCountResponse> getRatingDistribution(Long userId) {

        RatingCountResponse result = profileFavoriteService.findRatingDistributionByUserId(userId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_RATING_DISTRIBUTION_LOAD_SUCCESS, result);
    }

    // 선호 해시태그 조회
    public CustomResponse<FavoriteHashtagsResponse> getHashtags(Long userId) {

        FavoriteHashtagsResponse result = profileFavoriteService.findFavoriteHashtagsByUserId(userId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_FAVORITE_HASHTAGS_LOAD_SUCCESS, result);
    }
}
