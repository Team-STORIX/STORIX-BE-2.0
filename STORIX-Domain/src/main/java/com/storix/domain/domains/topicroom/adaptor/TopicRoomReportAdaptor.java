package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.repository.TopicRoomReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TopicRoomReportAdaptor {

    private final TopicRoomReportRepository topicRoomReportRepository;

    public long countByReportCaseId(Long reportCaseId) {
        return topicRoomReportRepository.countByReportCaseId(reportCaseId);
    }

    public List<TopicRoomReport> findAllByReportCaseId(Long reportCaseId) {
        return topicRoomReportRepository.findAllByReportCaseIdOrderByCreatedAtAsc(reportCaseId);
    }
}
