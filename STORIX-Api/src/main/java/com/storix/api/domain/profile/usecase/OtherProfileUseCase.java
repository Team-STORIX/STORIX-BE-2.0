package com.storix.api.domain.profile.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.genrescore.service.GenreScoreQueryService;
import com.storix.domain.domains.library.dto.StandardLibraryWorksInfo;
import com.storix.domain.domains.library.service.LibraryService;
import com.storix.domain.domains.preference.dto.GenreScoreInfo;
import com.storix.domain.domains.profile.dto.FavoriteHashtagsResponse;
import com.storix.domain.domains.profile.dto.OtherUserFavoriteWorksInfo;
import com.storix.domain.domains.profile.dto.OtherUserLibraryResponse;
import com.storix.domain.domains.profile.dto.OtherUserProfileInfo;
import com.storix.domain.domains.profile.dto.ProfileFavoriteWorksWrapperDto;
import com.storix.domain.domains.profile.dto.RatingCountResponse;
import com.storix.domain.domains.profile.service.OtherProfileService;
import com.storix.domain.domains.profile.service.ProfileService;
import com.storix.domain.domains.profile.service.ProfileFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class OtherProfileUseCase {

    private final OtherProfileService otherProfileService;
    private final ProfileService profileService;
    private final ProfileFavoriteService profileFavoriteService;
    private final GenreScoreQueryService genreScoreQueryService;
    private final LibraryService libraryService;

    public CustomResponse<OtherUserProfileInfo> getProfile(Long viewerId, Long targetUserId) {

        otherProfileService.getViewableUser(viewerId, targetUserId);
        OtherUserProfileInfo result = OtherUserProfileInfo.from(profileService.getReaderProfileInfoV2(targetUserId));
        return CustomResponse.onSuccess(SuccessCode.PROFILE_OTHER_USER_LOAD_SUCCESS, result);
    }

    public CustomResponse<ProfileFavoriteWorksWrapperDto<OtherUserFavoriteWorksInfo>> getFavoriteWorksList(Long viewerId, Long targetUserId, Pageable pageable) {

        otherProfileService.getViewableUser(viewerId, targetUserId);

        int totalFavoriteWorksCount = profileFavoriteService.findTotalFavoriteWorksCount(targetUserId);
        Slice<OtherUserFavoriteWorksInfo> favoriteWorksInfos =
                profileFavoriteService.findAllOtherUserFavoriteWorksInfo(targetUserId, viewerId, pageable);

        ProfileFavoriteWorksWrapperDto<OtherUserFavoriteWorksInfo> result
                = new ProfileFavoriteWorksWrapperDto<>(totalFavoriteWorksCount, favoriteWorksInfos);

        return CustomResponse.onSuccess(SuccessCode.PROFILE_FAVORITE_WORKS_LIST_LOAD_SUCCESS, result);
    }

    public CustomResponse<RatingCountResponse> getRatingDistribution(Long viewerId, Long targetUserId) {

        otherProfileService.getViewableUser(viewerId, targetUserId);

        RatingCountResponse result = profileFavoriteService.findRatingDistributionByUserId(targetUserId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_RATING_DISTRIBUTION_LOAD_SUCCESS, result);
    }

    public CustomResponse<List<GenreScoreInfo>> getGenreStats(Long viewerId, Long targetUserId) {

        otherProfileService.getViewableUser(viewerId, targetUserId);

        List<GenreScoreInfo> result = genreScoreQueryService.getRawScores(targetUserId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_GENRE_STATS_LOAD_SUCCESS, result);
    }

    public CustomResponse<FavoriteHashtagsResponse> getHashtags(Long viewerId, Long targetUserId) {

        otherProfileService.getViewableUser(viewerId, targetUserId);

        FavoriteHashtagsResponse result = profileFavoriteService.findFavoriteHashtagsByUserId(targetUserId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_FAVORITE_HASHTAGS_LOAD_SUCCESS, result);
    }

    public CustomResponse<OtherUserLibraryResponse> getLibrary(Long viewerId, Long targetUserId, Pageable pageable) {

        if (!otherProfileService.isLibraryPublic(viewerId, targetUserId)) {
            return CustomResponse.onSuccess(SuccessCode.PROFILE_OTHER_USER_LIBRARY_LOAD_SUCCESS,
                    OtherUserLibraryResponse.ofPrivate(pageable));
        }

        int totalReviewCount = libraryService.getTotalReviewCount(targetUserId);
        Slice<StandardLibraryWorksInfo> reviewedWorksInfos =
                libraryService.getReviewedWorksInfo(targetUserId, viewerId, pageable);

        return CustomResponse.onSuccess(SuccessCode.PROFILE_OTHER_USER_LIBRARY_LOAD_SUCCESS,
                OtherUserLibraryResponse.ofPublic(totalReviewCount, reviewedWorksInfos));
    }
}
