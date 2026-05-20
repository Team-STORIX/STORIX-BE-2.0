package com.storix.domain.domains.report.service;

import com.storix.domain.domains.feed.repository.FeedReplyReportRepository;
import com.storix.domain.domains.feed.repository.FeedReportRepository;
import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.dto.AdminReportListResponse;
import com.storix.domain.domains.report.dto.AdminReportSearchCondition;
import com.storix.domain.domains.review.repository.ReviewReportRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportQueryService {

    private final ReportCaseAdaptor reportCaseAdaptor;
    private final FeedReportRepository feedReportRepository;
    private final FeedReplyReportRepository feedReplyReportRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final TopicRoomReportRepository topicRoomReportRepository;

    public Page<AdminReportListResponse> getReports(AdminReportSearchCondition condition, Pageable pageable) {
        return reportCaseAdaptor.searchReportCases(condition, pageable)
                .map(reportCase -> AdminReportListResponse.from(reportCase, countReports(reportCase)));
    }

    public long countUnprocessedReports() {
        return reportCaseAdaptor.countByStatus(ReportStatus.RECEIVED);
    }

    private long countReports(ReportCase reportCase) {
        return switch (reportCase.getTargetType()) {
            case FEED -> feedReportRepository.countByReportCaseId(reportCase.getId());
            case FEED_REPLY -> feedReplyReportRepository.countByReportCaseId(reportCase.getId());
            case REVIEW -> reviewReportRepository.countByReportCaseId(reportCase.getId());
            case TOPIC_ROOM -> topicRoomReportRepository.countByReportCaseId(reportCase.getId());
        };
    }
}
