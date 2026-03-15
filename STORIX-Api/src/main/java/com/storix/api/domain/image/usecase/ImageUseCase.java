package com.storix.api.domain.image.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.image.dto.FileUploadRequest;
import com.storix.domain.domains.image.dto.PresignedUrlResponse;
import com.storix.domain.domains.image.dto.ProfileImageUploadRequest;
import com.storix.domain.domains.image.service.S3CacheHelper;
import com.storix.api.domain.image.helper.S3PresignHelper;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@Transactional
@RequiredArgsConstructor
public class ImageUseCase {

    private final S3PresignHelper s3PresignHelper;
    private final S3CacheHelper s3CacheHelper;

    public CustomResponse<List<PresignedUrlResponse>> getBoardImagePresignedUrl(AuthUserDetails authUserDetails, FileUploadRequest req) {

        String prefix = "public/board/reader";

        List<PresignedUrlResponse> results = req.files().stream()
                .map(file -> s3PresignHelper.createPresignedPutUrl(
                        authUserDetails.getUserId(), file.contentType(), prefix))
                .toList();

        List<String> objectKeys = results.stream()
                .map(PresignedUrlResponse::objectKey)
                .toList();
        s3CacheHelper.cacheBoardKeys(authUserDetails.getUserId(), objectKeys);

        return CustomResponse.onSuccess(SuccessCode.IMAGE_ISSUE_PRESIGNED_URL_SUCCESS, results);
    }

    public CustomResponse<PresignedUrlResponse> getProfileImagePresignedUrl(Long userId, ProfileImageUploadRequest req) {

        PresignedUrlResponse result = s3PresignHelper.createPresignedPutUrl(
                userId, req.file().contentType(), "public/profile");

        s3CacheHelper.cacheProfileKey(userId, result.objectKey());

        return CustomResponse.onSuccess(SuccessCode.IMAGE_ISSUE_PRESIGNED_URL_SUCCESS, result);
    }

    public CustomResponse<String> getImageUrl(String objectKey) {
        String presignedGetUrl = s3PresignHelper.createPresignedGetUrl(objectKey);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, presignedGetUrl);
    }

}
