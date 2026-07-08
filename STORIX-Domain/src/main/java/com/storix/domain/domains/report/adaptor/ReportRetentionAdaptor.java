package com.storix.domain.domains.report.adaptor;

import com.storix.domain.domains.feed.repository.FeedReplyReportRepository;
import com.storix.domain.domains.feed.repository.FeedReportRepository;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.repository.ReportCaseRepository;
import com.storix.domain.domains.review.repository.ReviewReportRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportRetentionAdaptor {

    private static final List<ReportStatus> PROCESSED_STATUSES =
            List.of(ReportStatus.COMPLETED, ReportStatus.REJECTED);

    private final ReportCaseRepository reportCaseRepository;
    private final FeedReportRepository feedReportRepository;
    private final FeedReplyReportRepository feedReplyReportRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final TopicRoomReportRepository topicRoomReportRepository;

    @Transactional
    public int purgeProcessedBefore(LocalDateTime cutoff) {
        List<Long> caseIds = reportCaseRepository.findIdsByProcessedAtBeforeAndStatusIn(cutoff, PROCESSED_STATUSES);
        if (caseIds.isEmpty()) {
            return 0;
        }
        feedReportRepository.deleteByReportCaseIdIn(caseIds);
        feedReplyReportRepository.deleteByReportCaseIdIn(caseIds);
        reviewReportRepository.deleteByReportCaseIdIn(caseIds);
        topicRoomReportRepository.deleteByReportCaseIdIn(caseIds);
        return reportCaseRepository.deleteByIdIn(caseIds);
    }
}
