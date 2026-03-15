package com.storix.domain.domains.review.service;

import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo;
import com.storix.domain.domains.review.adaptor.ReviewLikeAdaptor;
import com.storix.domain.domains.review.adaptor.ReviewReportAdaptor;
import com.storix.domain.domains.review.dto.ModifyReviewRequest;
import com.storix.domain.domains.review.dto.CreateWorksDetailReportCommand;
import com.storix.domain.domains.topicroom.domain.enums.ReportReason;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.topicroom.exception.SelfReportException;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import com.storix.domain.domains.works.exception.InvalidReviewReportException;
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
    public void reportReview(Long userId, Long reviewId, Long reportedUserId, ReportReason reason, String otherReason) {

        Long actualReviewerId = reviewAdaptor.findReviewerIdById(reviewId);
        if (!actualReviewerId.equals(reportedUserId)) {
            throw InvalidReviewReportException.EXCEPTION;
        }

        if (userId.equals(actualReviewerId)) {
            throw SelfReportException.EXCEPTION;
        }

        CreateWorksDetailReportCommand cmd = new CreateWorksDetailReportCommand(
                userId,
                actualReviewerId,
                reviewId,
                reason,
                otherReason
        );

        reviewReportAdaptor.saveReport(cmd);
    }

}
