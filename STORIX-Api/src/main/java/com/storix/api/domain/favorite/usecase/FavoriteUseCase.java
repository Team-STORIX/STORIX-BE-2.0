package com.storix.api.domain.favorite.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.favorite.service.FavoriteService;
import com.storix.domain.domains.favorite.dto.FavoriteWorksStatusResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.works.application.helper.AdultWorksHelper;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class FavoriteUseCase {

    private final FavoriteService favoriteService;
    private final AdultWorksHelper adultWorksHelper;

    // 관심 작품 등록 여부 조회
    public CustomResponse<FavoriteWorksStatusResponse> getFavoriteWorksStatus(AuthUserDetails authUserDetails, Long worksId) {

        Long userId = authUserDetails != null ? authUserDetails.getUserId() : null;

        // 성인 작품 여부 확인 및 핸들링
        adultWorksHelper.CheckUserAuthorityWithWorks(userId, worksId);

        // 비로그인 유저인 경우
        if (userId == null) {
            return CustomResponse.onSuccess(SuccessCode.FAVORITE_WORKS_LOAD_SUCCESS,
                    new FavoriteWorksStatusResponse(null));
        }

        // 로그인 유저인 경우
        boolean isFavoriteWorks = favoriteService.isFavoriteWorks(userId, worksId);
        FavoriteWorksStatusResponse result = new FavoriteWorksStatusResponse(isFavoriteWorks);
        return CustomResponse.onSuccess(SuccessCode.FAVORITE_WORKS_LOAD_SUCCESS, result);
    }

    // 관심 작품 등록
    public CustomResponse<FavoriteWorksStatusResponse> addWorksToFavorite(Long userId, Long worksId) {

        // 성인 작품 여부 확인 및 핸들링
        adultWorksHelper.CheckUserAuthorityWithWorks(userId, worksId);

        favoriteService.saveFavoriteWorks(userId, worksId);
        FavoriteWorksStatusResponse result = new FavoriteWorksStatusResponse(true);
        return CustomResponse.onSuccess(SuccessCode.FAVORITE_WORKS_ADD_SUCCESS, result);
    }

    // 관심 작품 등록 해제
    public CustomResponse<FavoriteWorksStatusResponse> deleteFavoriteWorks(Long userId, Long worksId) {

        // 성인 작품 여부 확인 및 핸들링
        adultWorksHelper.CheckUserAuthorityWithWorks(userId, worksId);

        favoriteService.deleteFavoriteWorks(userId, worksId);
        FavoriteWorksStatusResponse result = new FavoriteWorksStatusResponse(false);
        return CustomResponse.onSuccess(SuccessCode.FAVORITE_WORKS_DELETE_SUCCESS, result);
    }

}
