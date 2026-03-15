package com.storix.storix_api.domains.feed.adaptor;

import com.storix.storix_api.domains.feed.domain.FeedReplyReport;
import com.storix.storix_api.domains.feed.domain.FeedReport;
import com.storix.storix_api.domains.feed.dto.CreateFeedReportCommand;
import com.storix.storix_api.domains.feed.repository.FeedReplyReportRepository;
import com.storix.storix_api.domains.feed.repository.FeedReportRepository;
import com.storix.storix_api.global.apiPayload.exception.feed.DuplicateFeedReplyReportException;
import com.storix.storix_api.global.apiPayload.exception.feed.DuplicateFeedReportException;
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
