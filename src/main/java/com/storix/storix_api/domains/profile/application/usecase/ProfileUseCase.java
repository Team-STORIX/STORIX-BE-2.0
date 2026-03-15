package com.storix.storix_api.domains.profile.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.image.helper.S3CacheHelper;
import com.storix.storix_api.domains.profile.application.service.ProfileService;
import com.storix.storix_api.domains.profile.dto.UserInfo;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.user.domain.Role;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import com.storix.storix_api.global.apiPayload.exception.profile.ProfileImageNotExistException;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ProfileUseCase {

    private final ProfileService profileService;
    private final S3CacheHelper s3CacheHelper;

    // 기본 프로필 조회
    public CustomResponse<UserInfo> getUserProfile(AuthUserDetails authUserDetails) {

        String role = authUserDetails.getRole().getStringValue();
        Long userId = authUserDetails.getUserId();

        if (role.equals(String.valueOf(Role.READER))) {
            UserInfo readerProfileInfo = profileService.getReaderProfileInfo(userId);
            return CustomResponse.onSuccess(SuccessCode.PROFILE_LOAD_SUCCESS, readerProfileInfo);
        } else {
            UserInfo artistProfileInfo = profileService.getArtistProfileInfo(userId);
            return CustomResponse.onSuccess(SuccessCode.PROFILE_LOAD_SUCCESS, artistProfileInfo);
        }
    }

    // 독자 닉네임 변경
    public CustomResponse<String> changeNickName(String nickName, Long userId) {
        profileService.validNickname(nickName, userId);
        String newNickname = profileService.changeNickname(nickName, userId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_UPDATE_NICKNAME_SUCCESS, newNickname);
    }

    // 독자 닉네임 중복 체크
    public CustomResponse<Void> checkAvailableNickname(String nickName, Long userId) {
        profileService.validNickname(nickName, userId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_NICKNAME_SUCCESS);
    }

    // 독자 한 줄 소개 변경
    public CustomResponse<String> changeDescription(String profileDescription, Long userId) {
        String newProfileDescription = profileService.changeDescription(profileDescription, userId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_UPDATE_DESCRIPTION_SUCCESS, newProfileDescription);
    }

    // 프로필 사진 변경
    public CustomResponse<String> changeImage (String objectKey, Long userId) {
        if (!s3CacheHelper.isValidProfileKey(userId, objectKey)) {
            throw ProfileImageNotExistException.EXCEPTION;
        }
        String imageUrl = profileService.changeProfileImage(objectKey, userId);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_UPDATE_IMAGE_SUCCESS, imageUrl);
    }
}
