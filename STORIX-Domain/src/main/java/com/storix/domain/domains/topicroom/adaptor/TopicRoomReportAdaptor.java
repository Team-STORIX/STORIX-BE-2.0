package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.report.repository.ReportedUserCountProjection;
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

    public boolean hasAlreadyReported(Long reporterId, Long reportedUserId, Long topicRoomId, Long chatMessageId) {
        if (chatMessageId == null) {
            return topicRoomReportRepository.existsByReporterIdAndReportedUserIdAndTopicRoomIdAndChatMessageIdIsNull(
                    reporterId, reportedUserId, topicRoomId);
        }
        return topicRoomReportRepository.existsByReporterIdAndReportedUserIdAndTopicRoomIdAndChatMessageId(
                reporterId, reportedUserId, topicRoomId, chatMessageId);
    }

    public List<TopicRoomReport> findAllByReportCaseId(Long reportCaseId) {
        return topicRoomReportRepository.findAllByReportCaseIdOrderByCreatedAtAsc(reportCaseId);
    }

    public long countByReporterId(Long userId) {
        return topicRoomReportRepository.countByReporterId(userId);
    }

    public long countByReportedUserId(Long userId) {
        return topicRoomReportRepository.countByReportedUserId(userId);
    }

    public Map<Long, Long> countByReportedUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return topicRoomReportRepository.countByReportedUserIds(userIds).stream()
                .collect(Collectors.toMap(
                        ReportedUserCountProjection::getReportedUserId,
                        ReportedUserCountProjection::getReportCount
                ));
    }

}
