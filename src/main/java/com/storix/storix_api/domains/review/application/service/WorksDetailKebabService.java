package com.storix.storix_api.domains.review.application.service;

import com.storix.storix_api.domains.library.adaptor.LibraryAdaptor;
import com.storix.storix_api.domains.plus.adaptor.ReviewAdaptor;
import com.storix.storix_api.domains.plus.dto.ReviewedWorksIdAndRatingInfo;
import com.storix.storix_api.domains.review.adaptor.ReviewLikeAdaptor;
import com.storix.storix_api.domains.review.adaptor.ReviewReportAdaptor;
import com.storix.storix_api.domains.review.controller.dto.ModifyReviewRequest;
import com.storix.storix_api.domains.review.controller.dto.ReviewReportRequest;
import com.storix.storix_api.domains.review.dto.CreateWorksDetailReportCommand;
import com.storix.storix_api.domains.works.application.port.LoadWorksPort;
import com.storix.storix_api.global.apiPayload.exception.topicRoom.SelfReportException;
import com.storix.storix_api.global.apiPayload.exception.user.ForbiddenApproachException;
import com.storix.storix_api.global.apiPayload.exception.works.InvalidReviewReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorksDetailKebabService {

    private final ReviewAdaptor reviewAdaptor;
    private final ReviewLikeAdaptor reviewLikeAdaptor;
    private final ReviewReportAdaptor reviewReportAdaptor;
    private final LibraryAdaptor libraryAdaptor;

    private final LoadWorksPort loadWorksPort;

    @Transactional
    public Long changeReviewDetail(Long userId, Long reviewId, ModifyReviewRequest req) {
        Long reviewerId = reviewAdaptor.findReviewerIdById(reviewId);
        if (!reviewerId.equals(userId)) {
            throw ForbiddenApproachException.EXCEPTION;
        }

        return reviewAdaptor.updateReviewDetail(reviewId, req);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Long reviewerId = reviewAdaptor.findReviewerIdById(reviewId);
        if (!reviewerId.equals(userId)) {
            throw ForbiddenApproachException.EXCEPTION;
        }

        // 연관관계 맺은 리뷰 좋아요 삭제
        reviewLikeAdaptor.deleteAllRelatedReviewLike(reviewId);

        // 작품 평점 및 리뷰 개수 반영
        ReviewedWorksIdAndRatingInfo dto =
                reviewAdaptor.getReviewedWorksIdAndRatingInfo(reviewId);
        loadWorksPort.updateDecrementingReviewInfoToWorks(dto.worksId(), dto.rating().getRatingValue());

        // 서재 리뷰 작품 개수 반영
        libraryAdaptor.decrementReviewCount(userId);

        reviewAdaptor.deleteReview(userId, reviewId);
    }

    @Transactional
    public void reportReview(Long userId, Long reviewId, ReviewReportRequest req) {

        Long reportedUserId = reviewAdaptor.findReviewerIdById(reviewId);
        if (!reportedUserId.equals(req.reportedUserId())) {
            throw InvalidReviewReportException.EXCEPTION;
        }

        if (userId.equals(reportedUserId)) {
            throw SelfReportException.EXCEPTION;
        }

        CreateWorksDetailReportCommand cmd = new CreateWorksDetailReportCommand(
                userId,
                reportedUserId,
                reviewId,
                req.reason(),
                req.otherReason()
        );

        reviewReportAdaptor.saveReport(cmd);
    }

}
