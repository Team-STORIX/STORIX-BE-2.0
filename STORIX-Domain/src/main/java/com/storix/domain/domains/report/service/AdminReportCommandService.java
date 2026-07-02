package com.storix.domain.domains.report.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo;
import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.TargetContentType;
import com.storix.domain.domains.report.exception.AlreadyProcessedReportCaseException;
import com.storix.domain.domains.report.exception.InvalidReportProcessRequestException;
import com.storix.domain.domains.review.adaptor.ReviewLikeAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserSanctionHistoryAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.domain.UserSanctionHistory;
import com.storix.domain.domains.user.domain.UserSanctionSource;
import com.storix.domain.domains.user.domain.UserSanctionType;
import com.storix.domain.domains.user.domain.WithdrawReason;
import com.storix.domain.domains.user.publisher.UserAccessRevokedPublisher;
import com.storix.domain.domains.user.service.AuthService;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminReportCommandService {

    private static final int SUSPENSION_DAYS = 7;
    private static final String ACCOUNT_DELETION_DETAIL = "관리자 신고 처리로 인한 계정 삭제";

    private final ReportCaseAdaptor reportCaseAdaptor;
    private final ChatAdaptor chatAdaptor;
    private final BoardAdaptor boardAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final ReviewLikeAdaptor reviewLikeAdaptor;
    private final LibraryAdaptor libraryAdaptor;
    private final LoadWorksPort loadWorksPort;
    private final UserAdaptor userAdaptor;
    private final AuthService authService;
    private final UserAccessRevokedPublisher userAccessRevokedPublisher;
    private final UserSanctionHistoryAdaptor userSanctionHistoryAdaptor;

    @Transactional
    public void processReport(Long adminId, Long reportCaseId, ReportStatus status, ReportAction processAction, String processMemo) {
        validateRequest(status, processAction);

        ReportCase reportCase = reportCaseAdaptor.findByIdForUpdate(reportCaseId);

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
        if (status == ReportStatus.REJECTED && processAction != null) {
            throw InvalidReportProcessRequestException.EXCEPTION;
        }
        if (status == ReportStatus.COMPLETED && processAction == null) {
            throw InvalidReportProcessRequestException.EXCEPTION;
        }
    }

    private void executeAction(ReportCase reportCase, ReportAction action) {
        switch (action) {
            case CONTENT_DELETED -> deleteContent(reportCase);
            case ACCOUNT_SUSPENDED -> suspendUser(reportCase);
            case ACCOUNT_DELETED -> withdrawUserByAdminAction(reportCase);
        }
    }

    private void deleteContent(ReportCase reportCase) {
        Long targetId = reportCase.getTargetId();
        TargetContentType targetType = reportCase.getTargetType();

        switch (targetType) {
            case FEED -> {
                Long ownerId = boardAdaptor.adminDeleteReaderBoard(targetId);
                if (ownerId != null) libraryAdaptor.decrementBoardCount(ownerId);
            }
            case FEED_REPLY -> readerFeedAdaptor.adminDeleteReaderBoardReply(targetId);
            case REVIEW -> deleteReview(targetId);
            case TOPIC_ROOM -> throw InvalidReportProcessRequestException.EXCEPTION;
            case CHAT -> deleteChatMessage(reportCase);
        }
        saveSanctionHistory(reportCase, UserSanctionType.CONTENT_DELETED, LocalDateTime.now(), null);
    }

    private void deleteChatMessage(ReportCase reportCase) {
        int deletedCount = chatAdaptor.softDeleteTalkMessageBySender(reportCase.getTargetId(), reportCase.getReportedUserId());
        if (deletedCount == 0) {
            throw InvalidReportProcessRequestException.EXCEPTION;
        }
    }

    private void deleteReview(Long reviewId) {
        if (!reviewAdaptor.adminDeleteReview(reviewId)) return;

        Long reviewerId = reviewAdaptor.findReviewerIdById(reviewId);
        ReviewedWorksIdAndRatingInfo info = reviewAdaptor.getReviewedWorksIdAndRatingInfo(reviewId);
        reviewLikeAdaptor.deleteAllRelatedReviewLike(reviewId);
        loadWorksPort.updateDecrementingReviewInfoToWorks(info.worksId(), info.rating().getRatingValue());
        libraryAdaptor.decrementReviewCount(reviewerId);
    }

    private void suspendUser(ReportCase reportCase) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime suspendedUntil = now.plusDays(SUSPENSION_DAYS);
        User user = userAdaptor.findUserById(reportCase.getReportedUserId());
        user.suspend(suspendedUntil);
        userAccessRevokedPublisher.publishSuspended(reportCase.getReportedUserId(), suspendedUntil);
        saveSanctionHistory(reportCase, UserSanctionType.SUSPENDED, now, suspendedUntil);
    }

    private void withdrawUserByAdminAction(ReportCase reportCase) {
        authService.withDrawUser(
                reportCase.getReportedUserId(),
                Set.of(WithdrawReason.OTHER),
                StringUtils.hasText(reportCase.getProcessMemo()) ? reportCase.getProcessMemo().trim() : ACCOUNT_DELETION_DETAIL
        );
        saveSanctionHistory(reportCase, UserSanctionType.WITHDRAWN, LocalDateTime.now(), null);
    }

    private void saveSanctionHistory(
            ReportCase reportCase,
            UserSanctionType type,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        userSanctionHistoryAdaptor.save(UserSanctionHistory.builder()
                .userId(reportCase.getReportedUserId())
                .adminId(reportCase.getProcessedByAdminId())
                .type(type)
                .source(UserSanctionSource.REPORT)
                .reportCaseId(reportCase.getId())
                .startedAt(startedAt)
                .endedAt(endedAt)
                .memo(reportCase.getProcessMemo())
                .build());
    }
}
