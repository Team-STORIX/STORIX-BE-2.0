package com.storix.domain.domains.feed.adaptor;

import com.storix.domain.domains.feed.domain.FeedReplyReport;
import com.storix.domain.domains.feed.domain.FeedReport;
import com.storix.domain.domains.feed.dto.CreateFeedReportCommand;
import com.storix.domain.domains.feed.repository.FeedReplyReportRepository;
import com.storix.domain.domains.feed.repository.FeedReportRepository;
import com.storix.domain.domains.feed.exception.DuplicateFeedReplyReportException;
import com.storix.domain.domains.feed.exception.DuplicateFeedReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

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

}
