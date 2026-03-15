package com.storix.api.domain.plus.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.plus.service.ReviewService;
import com.storix.api.domain.plus.controller.dto.ReaderReviewUploadRequest;
import com.storix.domain.domains.plus.dto.CreateReviewCommand;
import com.storix.domain.domains.plus.dto.ReaderReviewRedirectResponse;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ReviewUseCase {

    private final ReviewService reviewService;

    public CustomResponse<ReaderReviewRedirectResponse> createReaderReview(Long userId, ReaderReviewUploadRequest req) {
        CreateReviewCommand cmd = new CreateReviewCommand(userId, req.worksId(), req.isSpoiler(), req.rating(), req.content());
        ReaderReviewRedirectResponse result = reviewService.createReview(cmd);
        return CustomResponse.onSuccess(SuccessCode.PLUS_REVIEW_UPLOAD_SUCCESS, result);
    }

    public CustomResponse<Void> checkDuplicateReview(Long userId, Long worksId) {
        reviewService.isReviewExist(userId, worksId);
        return CustomResponse.onSuccess(SuccessCode.PLUS_REVIEW_CHECK_SUCCESS);
    }

}
