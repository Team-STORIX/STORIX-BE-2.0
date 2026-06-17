package com.storix.domain.domains.report.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.feed.adaptor.FeedReportAdaptor;
import com.storix.domain.domains.feed.domain.FeedReplyReport;
import com.storix.domain.domains.feed.domain.FeedReport;
import com.storix.domain.domains.feed.domain.ReaderBoardReply;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.dto.ReviewInfo;
import com.storix.domain.domains.report.adaptor.ReportCaseAdaptor;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.domain.ReportStatus;
import com.storix.domain.domains.report.domain.ReportTargetType;
import com.storix.domain.domains.report.dto.AdminReportDetailResponse;
import com.storix.domain.domains.report.dto.AdminReportListResponse;
import com.storix.domain.domains.report.dto.AdminReportSearchCondition;
import com.storix.domain.domains.report.dto.AdminUserReportSummaryResponse;
import com.storix.domain.domains.review.adaptor.ReviewReportAdaptor;
import com.storix.domain.domains.review.domain.ReviewReport;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomPersistenceAdapter;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomReportAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportQueryService {

    private final ReportCaseAdaptor reportCaseAdaptor;
    private final FeedReportAdaptor feedReportAdaptor;
    private final ReviewReportAdaptor reviewReportAdaptor;
    private final TopicRoomReportAdaptor topicRoomReportAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final TopicRoomPersistenceAdapter topicRoomPersistenceAdapter;
    private final ChatAdaptor chatAdaptor;
    private final UserAdaptor userAdaptor;

    public Page<AdminReportListResponse> getReports(AdminReportSearchCondition condition, Pageable pageable) {
        Page<ReportCase> page = reportCaseAdaptor.searchReportCases(condition, pageable);
        List<ReportCase> reportCases = page.getContent();

        List<Long> reportedUserIds = reportCases.stream()
                .map(ReportCase::getReportedUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> nickNames = loadNickNames(reportedUserIds);
        Map<Long, Long> reportCounts = loadReportCounts(reportCases);

        return page.map(reportCase -> AdminReportListResponse.from(
                reportCase,
                reportCounts.getOrDefault(reportCase.getId(), 0L),
                nickNames.get(reportCase.getReportedUserId())
        ));
    }

    public long countUnprocessedReports() {
        return reportCaseAdaptor.countByStatus(ReportStatus.RECEIVED);
    }

    public AdminUserReportSummaryResponse getUserReportSummary(Long userId) {
        String nickName = Optional.ofNullable(
                        userAdaptor.findStandardProfileInfoByUserIds(List.of(userId)).get(userId))
                .map(StandardProfileInfo::nickName)
                .orElse(null);

        Map<ReportStatus, Long> counts = reportCaseAdaptor.countGroupByStatus(userId);
        long received = counts.getOrDefault(ReportStatus.RECEIVED, 0L);
        long completed = counts.getOrDefault(ReportStatus.COMPLETED, 0L);
        long rejected = counts.getOrDefault(ReportStatus.REJECTED, 0L);
        long total = received + completed + rejected;

        AdminReportSearchCondition condition = new AdminReportSearchCondition(null, null, null, null, userId);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminReportListResponse> recentPage = getReports(condition, pageable);

        return new AdminUserReportSummaryResponse(userId, nickName, total, received, completed, rejected, recentPage.getContent());
    }

    public AdminReportDetailResponse getReportDetail(Long reportCaseId) {
        ReportCase reportCase = reportCaseAdaptor.findById(reportCaseId);

        return switch (reportCase.getTargetType()) {
            case FEED -> getFeedReportDetail(reportCase);
            case FEED_REPLY -> getFeedReplyReportDetail(reportCase);
            case REVIEW -> getReviewReportDetail(reportCase);
            case TOPIC_ROOM -> getTopicRoomReportDetail(reportCase);
        };
    }

    private Map<Long, Long> loadReportCounts(List<ReportCase> reportCases) {
        if (reportCases.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> reportCounts = new HashMap<>();
        reportCounts.putAll(feedReportAdaptor.countFeedReportsByReportCaseIds(
                reportCaseIdsByTargetType(reportCases, ReportTargetType.FEED)
        ));
        reportCounts.putAll(feedReportAdaptor.countFeedReplyReportsByReportCaseIds(
                reportCaseIdsByTargetType(reportCases, ReportTargetType.FEED_REPLY)
        ));
        reportCounts.putAll(reviewReportAdaptor.countByReportCaseIds(
                reportCaseIdsByTargetType(reportCases, ReportTargetType.REVIEW)
        ));
        reportCounts.putAll(topicRoomReportAdaptor.countByReportCaseIds(
                reportCaseIdsByTargetType(reportCases, ReportTargetType.TOPIC_ROOM)
        ));
        return reportCounts;
    }

    private List<Long> reportCaseIdsByTargetType(List<ReportCase> reportCases, ReportTargetType targetType) {
        return reportCases.stream()
                .filter(reportCase -> reportCase.getTargetType() == targetType)
                .map(ReportCase::getId)
                .toList();
    }

    private AdminReportDetailResponse getFeedReportDetail(ReportCase reportCase) {
        List<FeedReport> reports = feedReportAdaptor.findFeedReportsByReportCaseId(reportCase.getId());
        ReaderBoard board = readerFeedAdaptor.findReaderBoardById(reportCase.getTargetId());
        Long reportedUserId = reports.isEmpty() ? reportCase.getReportedUserId() : reports.get(0).getReportedUserId();

        Map<Long, String> nickNames = loadNickNames(collectUserIds(
                reports.stream().map(FeedReport::getReporterId).toList(),
                List.of(reportedUserId, board.getUserId())
        ));

        List<AdminReportDetailResponse.ReportItem> reportItems = reports.stream()
                .map(report -> new AdminReportDetailResponse.ReportItem(
                        report.getId(),
                        report.getReporterId(),
                        nickName(report.getReporterId(), nickNames),
                        report.getReportedUserId(),
                        null,
                        null,
                        report.getCreatedAt()
                ))
                .toList();

        AdminReportDetailResponse.ReportedContent content = new AdminReportDetailResponse.ReportedContent(
                reportCase.getTargetType(),
                board.getId(),
                null,
                board.getUserId(),
                nickName(board.getUserId(), nickNames),
                board.getContent(),
                board.getCreatedAt(),
                List.of()
        );

        return detailResponse(
                reportCase,
                summary(reportedUserId, nickNames, feedLocation(board.getId()), null, null, reports.size(), firstReportedAt(reportItems)),
                reportItems,
                content
        );
    }

    private AdminReportDetailResponse getFeedReplyReportDetail(ReportCase reportCase) {
        List<FeedReplyReport> reports = feedReportAdaptor.findFeedReplyReportsByReportCaseId(reportCase.getId());
        ReaderBoardReply reply = readerFeedAdaptor.findReplyById(reportCase.getTargetId());
        Long reportedUserId = reports.isEmpty() ? reportCase.getReportedUserId() : reports.get(0).getReportedUserId();

        Map<Long, String> nickNames = loadNickNames(collectUserIds(
                reports.stream().map(FeedReplyReport::getReporterId).toList(),
                List.of(reportedUserId, reply.getUserId())
        ));

        List<AdminReportDetailResponse.ReportItem> reportItems = reports.stream()
                .map(report -> new AdminReportDetailResponse.ReportItem(
                        report.getId(),
                        report.getReporterId(),
                        nickName(report.getReporterId(), nickNames),
                        report.getReportedUserId(),
                        null,
                        null,
                        report.getCreatedAt()
                ))
                .toList();

        AdminReportDetailResponse.ReportedContent content = new AdminReportDetailResponse.ReportedContent(
                reportCase.getTargetType(),
                reply.getId(),
                reply.getBoardId(),
                reply.getUserId(),
                nickName(reply.getUserId(), nickNames),
                reply.getComment(),
                reply.getCreatedAt(),
                List.of()
        );

        return detailResponse(
                reportCase,
                summary(reportedUserId, nickNames, feedReplyLocation(reply.getBoardId(), reply.getId()), null, null, reports.size(), firstReportedAt(reportItems)),
                reportItems,
                content
        );
    }

    private AdminReportDetailResponse getReviewReportDetail(ReportCase reportCase) {
        List<ReviewReport> reports = reviewReportAdaptor.findAllByReportCaseId(reportCase.getId());
        ReviewInfo review = reviewAdaptor.findReviewById(reportCase.getTargetId());
        Long reportedUserId = reports.isEmpty() ? reportCase.getReportedUserId() : reports.get(0).getReportedUserId();

        Map<Long, String> nickNames = loadNickNames(collectUserIds(
                reports.stream().map(ReviewReport::getReporterId).toList(),
                List.of(reportedUserId, review.reviewerId())
        ));

        List<AdminReportDetailResponse.ReportItem> reportItems = reports.stream()
                .map(report -> new AdminReportDetailResponse.ReportItem(
                        report.getId(),
                        report.getReporterId(),
                        nickName(report.getReporterId(), nickNames),
                        report.getReportedUserId(),
                        reasonName(report.getReason()),
                        report.getOtherReason(),
                        report.getCreatedAt()
                ))
                .toList();

        AdminReportDetailResponse.ReportedContent content = new AdminReportDetailResponse.ReportedContent(
                reportCase.getTargetType(),
                review.reviewId(),
                review.worksId(),
                review.reviewerId(),
                nickName(review.reviewerId(), nickNames),
                review.content(),
                review.createdAt(),
                List.of()
        );

        ReviewReport firstReport = reports.isEmpty() ? null : reports.get(0);

        return detailResponse(
                reportCase,
                summary(
                        reportedUserId, nickNames, reviewLocation(review.reviewId()),
                        firstReport != null ? reasonName(firstReport.getReason()) : null,
                        firstReport != null ? firstReport.getOtherReason() : null,
                        reports.size(), firstReportedAt(reportItems)
                ),
                reportItems,
                content
        );
    }

    private AdminReportDetailResponse getTopicRoomReportDetail(ReportCase reportCase) {
        List<TopicRoomReport> reports = topicRoomReportAdaptor.findAllByReportCaseId(reportCase.getId());
        TopicRoom room = topicRoomPersistenceAdapter.findById(reportCase.getTargetId());
        Long reportedUserId = reports.isEmpty() ? reportCase.getReportedUserId() : reports.get(0).getReportedUserId();

        List<ChatMessageResponseDto> chatMessages = reportedUserId == null
                ? List.of()
                : chatAdaptor.loadRecentMessagesBySender(room.getId(), reportedUserId, PageRequest.of(0, 20));

        Map<Long, String> nickNames = loadNickNames(collectUserIds(
                reports.stream().map(TopicRoomReport::getReporterId).toList(),
                collectUserIds(
                        Collections.singletonList(reportedUserId),
                        chatMessages.stream().map(ChatMessageResponseDto::senderId).toList()
                )
        ));

        List<AdminReportDetailResponse.ReportItem> reportItems = reports.stream()
                .map(report -> new AdminReportDetailResponse.ReportItem(
                        report.getId(),
                        report.getReporterId(),
                        nickName(report.getReporterId(), nickNames),
                        report.getReportedUserId(),
                        reasonName(report.getReason()),
                        report.getOtherReason(),
                        report.getCreatedAt()
                ))
                .toList();

        List<AdminReportDetailResponse.ReportedChatMessage> reportedChatMessages = chatMessages.stream()
                .map(message -> new AdminReportDetailResponse.ReportedChatMessage(
                        message.id(),
                        message.senderId(),
                        message.senderName(),
                        message.message(),
                        message.messageType(),
                        message.createdAt()
                ))
                .toList();

        AdminReportDetailResponse.ReportedContent content = new AdminReportDetailResponse.ReportedContent(
                reportCase.getTargetType(),
                room.getId(),
                room.getWorksId(),
                reportedUserId,
                nickName(reportedUserId, nickNames),
                room.getTopicRoomName(),
                room.getCreatedAt(),
                reportedChatMessages
        );

        TopicRoomReport firstReport = reports.isEmpty() ? null : reports.get(0);

        return detailResponse(
                reportCase,
                summary(
                        reportedUserId, nickNames, topicRoomLocation(room.getId()),
                        firstReport != null ? reasonName(firstReport.getReason()) : null,
                        firstReport != null ? firstReport.getOtherReason() : null,
                        reports.size(), firstReportedAt(reportItems)
                ),
                reportItems,
                content
        );
    }

    private AdminReportDetailResponse detailResponse(
            ReportCase reportCase,
            AdminReportDetailResponse.Summary summary,
            List<AdminReportDetailResponse.ReportItem> reports,
            AdminReportDetailResponse.ReportedContent reportedContent
    ) {
        return new AdminReportDetailResponse(
                reportCase.getId(),
                reportCase.getTargetType(),
                reportCase.getTargetId(),
                reportCase.getStatus(),
                reportCase.getProcessedByAdminId(),
                reportCase.getProcessMemo(),
                reportCase.getProcessAction(),
                reportCase.getCreatedAt(),
                reportCase.getProcessedAt(),
                reportCase.hasPreviousProcessHistory(),
                summary,
                reports,
                reportedContent
        );
    }

    private AdminReportDetailResponse.Summary summary(
            Long reportedUserId,
            Map<Long, String> nickNames,
            String location,
            String reason,
            String otherReason,
            long reportCount,
            LocalDateTime firstReportedAt
    ) {
        return new AdminReportDetailResponse.Summary(
                reportedUserId,
                nickName(reportedUserId, nickNames),
                location,
                reason,
                otherReason,
                reportCount,
                firstReportedAt
        );
    }

    private String reasonName(Enum<?> reason) {
        return reason == null ? null : reason.name();
    }

    @SafeVarargs
    private final List<Long> collectUserIds(List<Long>... userIdGroups) {
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        for (List<Long> userIdGroup : userIdGroups) {
            if (userIdGroup != null) {
                userIdGroup.stream()
                        .filter(Objects::nonNull)
                        .forEach(userIds::add);
            }
        }
        return List.copyOf(userIds);
    }

    private Map<Long, String> loadNickNames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        return userAdaptor.findStandardProfileInfoByUserIds(List.copyOf(userIds)).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().nickName()
                ));
    }

    private String nickName(Long userId, Map<Long, String> nickNames) {
        if (userId == null) {
            return null;
        }
        return nickNames.get(userId);
    }

    private LocalDateTime firstReportedAt(List<AdminReportDetailResponse.ReportItem> reportItems) {
        if (reportItems.isEmpty()) {
            return null;
        }
        return reportItems.get(0).reportedAt();
    }

    private String feedLocation(Long boardId) {
        return "/api/v1/feed/reader/board/" + boardId;
    }

    private String feedReplyLocation(Long boardId, Long replyId) {
        return "/api/v1/feed/reader/board/" + boardId + "/reply/" + replyId;
    }

    private String reviewLocation(Long reviewId) {
        return "/api/v1/works/review/" + reviewId;
    }

    private String topicRoomLocation(Long roomId) {
        return "/api/v1/topic-rooms/" + roomId;
    }
}
