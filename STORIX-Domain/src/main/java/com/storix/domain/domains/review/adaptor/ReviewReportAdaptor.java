package com.storix.domain.domains.review.adaptor;

import com.storix.domain.domains.review.domain.ReviewReport;
import com.storix.domain.domains.review.dto.CreateWorksDetailReportCommand;
import com.storix.domain.domains.review.repository.ReportCaseCountProjection;
import com.storix.domain.domains.review.repository.ReviewReportRepository;
import com.storix.domain.domains.works.exception.DuplicateReviewReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<Long, Long> countByReportCaseIds(List<Long> reportCaseIds) {
        if (reportCaseIds == null || reportCaseIds.isEmpty()) {
            return Map.of();
        }
        return reviewReportRepository.countByReportCaseIds(reportCaseIds).stream()
                .collect(Collectors.toMap(
                        ReportCaseCountProjection::getReportCaseId,
                        ReportCaseCountProjection::getReportCount
                ));
    }

    public List<ReviewReport> findAllByReportCaseId(Long reportCaseId) {
        return reviewReportRepository.findAllByReportCaseIdOrderByCreatedAtAsc(reportCaseId);
    }
}
