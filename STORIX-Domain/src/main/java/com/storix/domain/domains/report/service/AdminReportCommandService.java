package com.storix.domain.domains.report.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo;
import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.report.exception.AlreadyProcessedReportCaseException;
import com.storix.domain.domains.report.exception.InvalidReportProcessRequestException;
import com.storix.domain.domains.review.adaptor.ReviewLikeAdaptor;
import com.storix.domain.domains.user.adaptor.TokenAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.service.AuthService;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminReportCommandService {

    private static final int SUSPENSION_DAYS = 7;

    private final ReportCaseAdaptor reportCaseAdaptor;
    private final BoardAdaptor boardAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final ReviewLikeAdaptor reviewLikeAdaptor;
    private final LibraryAdaptor libraryAdaptor;
    private final LoadWorksPort loadWorksPort;
    private final TokenAdaptor tokenAdaptor;
    private final UserAdaptor userAdaptor;
    private final AuthService authService;

    @Transactional
    public void processReport(Long adminId, Long reportCaseId, ReportStatus status, ReportAction processAction, String processMemo) {
        validateRequest(status, processAction);

        ReportCase reportCase = reportCaseAdaptor.findById(reportCaseId);

        if (reportCase.getStatus() != ReportStatus.RECEIVED) {
            throw AlreadyProcessedReportCaseException.EXCEPTION;
        }

        reportCase.process(status, processAction, processMemo, adminId);

        if (status == ReportStatus.COMPLETED && processAction != null) {
            executeAction(reportCase, processAction);
        }
    }

    private void validateRequest(ReportStatus status, ReportAction processAction) {
        if (status == ReportStatus.RECEIVED) {
            throw InvalidReportProcessRequestException.EXCEPTION;
        }
        if (status == ReportStatus.COMPLETED && processAction == null) {
            throw InvalidReportProcessRequestException.EXCEPTION;
        }
    }

    private void executeAction(ReportCase reportCase, ReportAction action) {
        switch (action) {
            case CONTENT_DELETED -> deleteContent(reportCase);
            case ACCOUNT_SUSPENDED -> suspendUser(reportCase.getReportedUserId());
            case ACCOUNT_DELETED -> authService.withDrawUser(reportCase.getReportedUserId());
        }
    }

    private void deleteContent(ReportCase reportCase) {
        Long targetId = reportCase.getTargetId();
        ReportTargetType targetType = reportCase.getTargetType();

        switch (targetType) {
            case FEED -> {
                Long ownerId = boardAdaptor.adminDeleteReaderBoard(targetId);
                libraryAdaptor.decrementBoardCount(ownerId);
            }
            case FEED_REPLY -> readerFeedAdaptor.adminDeleteReaderBoardReply(targetId);
            case REVIEW -> deleteReview(targetId);
            case TOPIC_ROOM -> {
                // 채팅 메시지 삭제 미구현 — 처리 기록만 남김
            }
        }
    }

    private void deleteReview(Long reviewId) {
        Long reviewerId = reviewAdaptor.findReviewerIdById(reviewId);
        ReviewedWorksIdAndRatingInfo info = reviewAdaptor.getReviewedWorksIdAndRatingInfo(reviewId);

        reviewLikeAdaptor.deleteAllRelatedReviewLike(reviewId);
        loadWorksPort.updateDecrementingReviewInfoToWorks(info.worksId(), info.rating().getRatingValue());
        libraryAdaptor.decrementReviewCount(reviewerId);
        reviewAdaptor.adminDeleteReview(reviewId);
    }

    private void suspendUser(Long userId) {
        User user = userAdaptor.findUserById(userId);
        user.suspend(LocalDateTime.now().plusDays(SUSPENSION_DAYS));
        tokenAdaptor.deleteRefreshTokenByUserIdIfPresent(userId);
    }
}
