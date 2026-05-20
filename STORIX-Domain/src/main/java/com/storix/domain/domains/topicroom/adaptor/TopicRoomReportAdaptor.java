package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.repository.ReportCaseCountProjection;
import com.storix.domain.domains.topicroom.repository.TopicRoomReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TopicRoomReportAdaptor {

    private final TopicRoomReportRepository topicRoomReportRepository;

    public Map<Long, Long> countByReportCaseIds(List<Long> reportCaseIds) {
        if (reportCaseIds == null || reportCaseIds.isEmpty()) {
            return Map.of();
        }
        return topicRoomReportRepository.countByReportCaseIds(reportCaseIds).stream()
                .collect(Collectors.toMap(
                        ReportCaseCountProjection::getReportCaseId,
                        ReportCaseCountProjection::getReportCount
                ));
    }

    public List<TopicRoomReport> findAllByReportCaseId(Long reportCaseId) {
        return topicRoomReportRepository.findAllByReportCaseIdOrderByCreatedAtAsc(reportCaseId);
    }
}
