package com.storix.domain.domains.feed.adaptor;

import com.storix.domain.domains.feed.domain.FeedReplyReport;
import com.storix.domain.domains.feed.domain.FeedReport;
import com.storix.domain.domains.feed.dto.CreateFeedReportCommand;
import com.storix.domain.domains.feed.exception.DuplicateFeedReplyReportException;
import com.storix.domain.domains.feed.exception.DuplicateFeedReportException;
import com.storix.domain.domains.feed.repository.FeedReplyReportRepository;
import com.storix.domain.domains.feed.repository.FeedReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public long countFeedReportsByReportCaseId(Long reportCaseId) {
        return feedReportRepository.countByReportCaseId(reportCaseId);
    }

    public List<FeedReport> findFeedReportsByReportCaseId(Long reportCaseId) {
        return feedReportRepository.findAllByReportCaseIdOrderByCreatedAtAsc(reportCaseId);
    }

    public long countFeedReplyReportsByReportCaseId(Long reportCaseId) {
        return feedReplyReportRepository.countByReportCaseId(reportCaseId);
    }

    public List<FeedReplyReport> findFeedReplyReportsByReportCaseId(Long reportCaseId) {
        return feedReplyReportRepository.findAllByReportCaseIdOrderByCreatedAtAsc(reportCaseId);
    }
}
