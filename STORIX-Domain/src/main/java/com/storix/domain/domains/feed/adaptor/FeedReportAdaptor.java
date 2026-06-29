package com.storix.domain.domains.feed.adaptor;

import com.storix.domain.domains.feed.domain.FeedReplyReport;
import com.storix.domain.domains.feed.domain.FeedReport;
import com.storix.domain.domains.feed.dto.CreateFeedReportCommand;
import com.storix.domain.domains.feed.exception.DuplicateFeedReplyReportException;
import com.storix.domain.domains.feed.exception.DuplicateFeedReportException;
import com.storix.domain.domains.feed.repository.FeedReplyReportRepository;
import com.storix.domain.domains.feed.repository.FeedReportRepository;
import com.storix.domain.domains.feed.repository.ReportCaseCountProjection;
import com.storix.domain.domains.user.dto.AdminUserReportItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeedReportAdaptor {

    private final FeedReportRepository feedReportRepository;
    private final FeedReplyReportRepository feedReplyReportRepository;

    public void saveReport(CreateFeedReportCommand cmd) {
        try {
            FeedReport feedReport = cmd.toEntity();
            feedReportRepository.save(feedReport);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateFeedReportException.EXCEPTION;
        }
    }

    public void saveReplyReport(CreateFeedReportCommand cmd) {
        try {
            FeedReplyReport feedReplyReport = cmd.toReplyEntity();
            feedReplyReportRepository.save(feedReplyReport);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateFeedReplyReportException.EXCEPTION;
        }
    }

    public boolean hasAlreadyReported(Long userId, Long boardId) {
        return feedReportRepository.existsByReporterIdAndBoardId(userId, boardId);
    }

    public boolean hasAlreadyReplyReported(Long userId, Long replyId) {
        return feedReplyReportRepository.existsByReporterIdAndReplyId(userId, replyId);
    }

    public Map<Long, Long> countFeedReportsByReportCaseIds(List<Long> reportCaseIds) {
        if (reportCaseIds == null || reportCaseIds.isEmpty()) {
            return Map.of();
        }
        return toCountMap(feedReportRepository.countByReportCaseIds(reportCaseIds));
    }

    public List<FeedReport> findFeedReportsByReportCaseId(Long reportCaseId) {
        return feedReportRepository.findAllByReportCaseIdOrderByCreatedAtAsc(reportCaseId);
    }

    public Map<Long, Long> countFeedReplyReportsByReportCaseIds(List<Long> reportCaseIds) {
        if (reportCaseIds == null || reportCaseIds.isEmpty()) {
            return Map.of();
        }
        return toCountMap(feedReplyReportRepository.countByReportCaseIds(reportCaseIds));
    }

    public List<FeedReplyReport> findFeedReplyReportsByReportCaseId(Long reportCaseId) {
        return feedReplyReportRepository.findAllByReportCaseIdOrderByCreatedAtAsc(reportCaseId);
    }

    public long countAllByReporterId(Long userId) {
        return feedReportRepository.countByReporterId(userId)
                + feedReplyReportRepository.countByReporterId(userId);
    }

    public long countAllByReportedUserId(Long userId) {
        return feedReportRepository.countByReportedUserId(userId)
                + feedReplyReportRepository.countByReportedUserId(userId);
    }

    public List<AdminUserReportItemResponse> findAdminReportsByReporterId(Long userId) {
        List<AdminUserReportItemResponse> reports = new java.util.ArrayList<>();
        reports.addAll(feedReportRepository.findAdminReportsByReporterId(userId));
        reports.addAll(feedReplyReportRepository.findAdminReportsByReporterId(userId));
        return reports;
    }

    public List<AdminUserReportItemResponse> findAdminReportsByReportedUserId(Long userId) {
        List<AdminUserReportItemResponse> reports = new java.util.ArrayList<>();
        reports.addAll(feedReportRepository.findAdminReportsByReportedUserId(userId));
        reports.addAll(feedReplyReportRepository.findAdminReportsByReportedUserId(userId));
        return reports;
    }

    private Map<Long, Long> toCountMap(List<ReportCaseCountProjection> rows) {
        return rows.stream()
                .collect(Collectors.toMap(
                        ReportCaseCountProjection::getReportCaseId,
                        ReportCaseCountProjection::getReportCount
                ));
    }
}
