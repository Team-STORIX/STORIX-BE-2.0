package com.storix.domain.domains.review.adaptor;

import com.storix.domain.domains.review.domain.ReviewReport;
import com.storix.domain.domains.review.dto.CreateWorksDetailReportCommand;
import com.storix.domain.domains.review.repository.ReviewReportRepository;
import com.storix.domain.domains.works.exception.DuplicateReviewReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewReportAdaptor {

    private final ReviewReportRepository reviewReportRepository;

    public void saveReport(CreateWorksDetailReportCommand cmd) {
        try {
            ReviewReport reviewReport = cmd.toEntity();
            reviewReportRepository.save(reviewReport);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateReviewReportException.EXCEPTION;
        }
    }

}
