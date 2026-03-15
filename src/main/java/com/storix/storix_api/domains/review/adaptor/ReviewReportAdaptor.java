package com.storix.storix_api.domains.review.adaptor;

import com.storix.storix_api.domains.review.domain.ReviewReport;
import com.storix.storix_api.domains.review.dto.CreateWorksDetailReportCommand;
import com.storix.storix_api.domains.review.repository.ReviewReportRepository;
import com.storix.storix_api.global.apiPayload.exception.works.DuplicateReviewReportException;
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
