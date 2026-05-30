package com.storix.domain.domains.report.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportAction;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.exception.AlreadyProcessedReportException;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportProcessService {

    private final ReportCaseAdaptor reportCaseAdaptor;
    private final UserAdaptor userAdaptor;
    private final BoardAdaptor boardAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;

    @Transactional
    public void process(Long reportCaseId, ReportStatus status, ReportAction processAction, String processMemo, Long adminId) {
        ReportCase reportCase = reportCaseAdaptor.findById(reportCaseId);

        if (reportCase.getStatus() != ReportStatus.RECEIVED) {
            throw AlreadyProcessedReportException.EXCEPTION;
        }

        reportCase.process(status, processAction, processMemo, adminId);

        if (status == ReportStatus.COMPLETED && processAction != null) {
            executeAction(reportCase, processAction);
        }
    }

    private void executeAction(ReportCase reportCase, ReportAction action) {
        switch (action) {
            case CONTENT_DELETED -> deleteContent(reportCase);
            case ACCOUNT_SUSPENDED -> suspendUser(reportCase.getReportedUserId());
            case ACCOUNT_DELETED -> deleteUser(reportCase.getReportedUserId());
        }
    }

    private void deleteContent(ReportCase reportCase) {
        long targetId = reportCase.getTargetId();
        switch (reportCase.getTargetType()) {
            case FEED -> boardAdaptor.adminDeleteReaderBoard(targetId);
            case FEED_REPLY -> readerFeedAdaptor.adminDeleteReaderBoardReply(targetId);
            case REVIEW -> reviewAdaptor.adminDeleteReview(targetId);
            case TOPIC_ROOM -> { /* 토픽룸 자체는 삭제 대상 아님 */ }
        }
    }

    private void suspendUser(Long reportedUserId) {
        if (reportedUserId == null) return;
        User user = userAdaptor.findUserById(reportedUserId);
        user.suspend();
    }

    private void deleteUser(Long reportedUserId) {
        if (reportedUserId == null) return;
        User user = userAdaptor.findUserById(reportedUserId);
        user.withdraw();
    }
}
