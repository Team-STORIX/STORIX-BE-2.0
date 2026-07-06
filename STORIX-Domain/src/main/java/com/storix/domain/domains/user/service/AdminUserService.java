package com.storix.domain.domains.user.service;

import com.storix.domain.domains.feed.adaptor.FeedReportAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo;
import com.storix.domain.domains.report.domain.TargetContentType;
import com.storix.domain.domains.review.adaptor.ReviewLikeAdaptor;
import com.storix.domain.domains.review.adaptor.ReviewReportAdaptor;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomReportAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlacklistAdaptor;
import com.storix.domain.domains.user.adaptor.UserSanctionHistoryAdaptor;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.domain.UserSanctionHistory;
import com.storix.domain.domains.user.domain.UserSanctionSource;
import com.storix.domain.domains.user.domain.UserSanctionType;
import com.storix.domain.domains.user.domain.WithdrawReason;
import com.storix.domain.domains.user.dto.AdminUserActivityStats;
import com.storix.domain.domains.user.dto.AdminUserBasicInfo;
import com.storix.domain.domains.user.dto.AdminUserContentItemResponse;
import com.storix.domain.domains.user.dto.AdminUserContentKey;
import com.storix.domain.domains.user.dto.AdminUserContentPageResponse;
import com.storix.domain.domains.user.dto.AdminUserListResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionDetailResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionHistoryResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionPageResponse;
import com.storix.domain.domains.user.dto.AdminUserSearchCondition;
import com.storix.domain.domains.user.exception.admin.InvalidAdminUserSanctionRequestException;
import com.storix.domain.domains.user.exception.admin.UserAlreadySuspendedException;
import com.storix.domain.domains.user.exception.admin.UserNotSuspendedException;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import com.storix.domain.domains.user.exception.auth.InvalidWithdrawException;
import com.storix.domain.domains.user.publisher.UserAccessRevokedPublisher;
import com.storix.domain.domains.user.repository.AdminUserContentQueryRepository;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final int SUSPENSION_DAYS = 7;
    private static final String ACCOUNT_DELETION_DETAIL = "관리자 처리로 인한 계정 삭제";

    private final UserAdaptor userAdaptor;
    private final AuthService authService;
    private final UserAccessRevokedPublisher userAccessRevokedPublisher;
    private final UserBlacklistAdaptor userBlacklistAdaptor;
    private final BoardAdaptor boardAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final ChatAdaptor chatAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final ReviewLikeAdaptor reviewLikeAdaptor;
    private final LibraryAdaptor libraryAdaptor;
    private final WorksAdaptor worksAdaptor;
    private final FeedReportAdaptor feedReportAdaptor;
    private final ReviewReportAdaptor reviewReportAdaptor;
    private final TopicRoomReportAdaptor topicRoomReportAdaptor;
    private final TopicRoomAdaptor topicRoomAdaptor;
    private final UserSanctionHistoryAdaptor userSanctionHistoryAdaptor;
    private final AdminUserContentQueryRepository adminUserContentQueryRepository;

    public Page<AdminUserListResponse> searchUsers(AdminUserSearchCondition condition, Pageable pageable) {
        String nickName = StringUtils.hasText(condition.nickName()) ? condition.nickName().trim() : null;
        Page<AdminUserListResponse> page = userAdaptor.findAdminUsers(
                condition.userId(),
                nickName,
                condition.accountState(),
                pageable
        );

        Map<Long, Long> reportedCounts = countReportedByUserIds(page.getContent().stream()
                .map(AdminUserListResponse::userId)
                .toList());

        return page.map(user -> new AdminUserListResponse(
                user.userId(),
                user.nickName(),
                user.email(),
                user.oauthProvider(),
                user.joinedAt(),
                user.accountState(),
                user.suspendedUntil(),
                user.lastLoginAt(),
                reportedCounts.getOrDefault(user.userId(), 0L)
        ));
    }

    public AdminUserBasicInfo getBasicInfo(Long userId) {
        User user = userAdaptor.findUserById(userId);
        return AdminUserBasicInfo.from(user);
    }

    public AdminUserActivityStats getActivityStats(Long userId) {
        return new AdminUserActivityStats(
                boardAdaptor.countActiveBoardsByUserId(userId),
                readerFeedAdaptor.countActiveRepliesByUserId(userId),
                topicRoomAdaptor.countJoinedRooms(userId),
                reviewAdaptor.countActiveReviewsByUserId(userId),
                countReportedByUserId(userId)
        );
    }

    private long countReportedByUserId(Long userId) {
        return feedReportAdaptor.countAllByReportedUserId(userId)
                + reviewReportAdaptor.countByReportedUserId(userId)
                + topicRoomReportAdaptor.countByReportedUserId(userId);
    }

    private Map<Long, Long> countReportedByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> counts = new java.util.HashMap<>();
        feedReportAdaptor.countAllByReportedUserIds(userIds)
                .forEach((userId, count) -> counts.merge(userId, count, Long::sum));
        reviewReportAdaptor.countByReportedUserIds(userIds)
                .forEach((userId, count) -> counts.merge(userId, count, Long::sum));
        topicRoomReportAdaptor.countByReportedUserIds(userIds)
                .forEach((userId, count) -> counts.merge(userId, count, Long::sum));
        return counts;
    }

    public List<AdminUserSanctionHistoryResponse> getSanctionHistories(Long userId) {
        return userSanctionHistoryAdaptor.findAllByUserId(userId).stream()
                .map(AdminUserSanctionHistoryResponse::from)
                .toList();
    }

    public AdminUserSanctionPageResponse getSanctionDetails(Long userId, Pageable pageable) {
        Page<UserSanctionHistory> page = userSanctionHistoryAdaptor.findPageByUserId(userId, pageable);
        List<Long> adminIds = page.getContent().stream()
                .map(UserSanctionHistory::getAdminId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> adminNickNames = userAdaptor.findNicknameMapByUserIds(adminIds);
        List<AdminUserSanctionDetailResponse> content = page.getContent().stream()
                .map(history -> AdminUserSanctionDetailResponse.from(history, adminNickNames))
                .toList();
        return AdminUserSanctionPageResponse.from(page, content);
    }

    public AdminUserContentPageResponse getUserContents(Long userId, Pageable pageable) {
        userAdaptor.findUserById(userId);

        List<AdminUserContentKey> keys = adminUserContentQueryRepository.findContentKeys(userId, pageable);
        Map<TargetContentType, List<Long>> idsByType = keys.stream()
                .collect(Collectors.groupingBy(
                        AdminUserContentKey::type,
                        () -> new EnumMap<>(TargetContentType.class),
                        Collectors.mapping(AdminUserContentKey::contentId, Collectors.toList())
                ));
        Map<ContentLookupKey, AdminUserContentItemResponse> contentsByKey = loadAdminUserContents(idsByType);
        List<AdminUserContentItemResponse> contents = keys.stream()
                .map(key -> contentsByKey.get(new ContentLookupKey(key.type(), key.contentId())))
                .filter(Objects::nonNull)
                .toList();

        long totalElements = countUserContents(userId);
        return AdminUserContentPageResponse.from(new PageImpl<>(
                contents,
                pageable,
                totalElements
        ));
    }

    private Map<ContentLookupKey, AdminUserContentItemResponse> loadAdminUserContents(
            Map<TargetContentType, List<Long>> idsByType
    ) {
        Map<ContentLookupKey, AdminUserContentItemResponse> contents = new java.util.HashMap<>();
        List<Long> feedIds = idsByType.getOrDefault(TargetContentType.FEED, Collections.emptyList());
        if (!feedIds.isEmpty()) {
            putAll(contents, TargetContentType.FEED, boardAdaptor.findAdminBoardContentsByIds(feedIds));
        }

        List<Long> replyIds = idsByType.getOrDefault(TargetContentType.FEED_REPLY, Collections.emptyList());
        if (!replyIds.isEmpty()) {
            putAll(contents, TargetContentType.FEED_REPLY, readerFeedAdaptor.findAdminReplyContentsByIds(replyIds));
        }

        List<Long> chatIds = idsByType.getOrDefault(TargetContentType.CHAT, Collections.emptyList());
        if (!chatIds.isEmpty()) {
            putAll(contents, TargetContentType.CHAT, chatAdaptor.findAdminChatContentsByIds(chatIds));
        }

        List<Long> reviewIds = idsByType.getOrDefault(TargetContentType.REVIEW, Collections.emptyList());
        if (!reviewIds.isEmpty()) {
            putAll(contents, TargetContentType.REVIEW, reviewAdaptor.findAdminReviewContentsByIds(reviewIds));
        }
        return contents;
    }

    private void putAll(
            Map<ContentLookupKey, AdminUserContentItemResponse> contents,
            TargetContentType type,
            List<AdminUserContentItemResponse> items
    ) {
        items.stream()
                .collect(Collectors.toMap(
                        item -> new ContentLookupKey(type, item.contentId()),
                        Function.identity()
                ))
                .forEach(contents::put);
    }

    private long countUserContents(Long userId) {
        return boardAdaptor.countActiveBoardsByUserId(userId)
                + readerFeedAdaptor.countActiveRepliesByUserId(userId)
                + chatAdaptor.countActiveChatsByUserId(userId)
                + reviewAdaptor.countActiveReviewsByUserId(userId);
    }

    private record ContentLookupKey(TargetContentType type, Long contentId) {
    }

    @Transactional
    public void processManualSanction(
            Long adminId,
            Long userId,
            UserSanctionType type,
            TargetContentType targetType,
            Long targetId,
            String memo
    ) {
        switch (type) {
            case SUSPENDED -> suspendUserManually(adminId, userId, memo);
            case WITHDRAWN -> withdrawUserManually(adminId, userId, memo);
            case RESTORED -> restoreUserManually(adminId, userId, memo);
            case CONTENT_DELETED -> deleteContentManually(adminId, userId, targetType, targetId, memo);
        }
    }

    private void suspendUserManually(Long adminId, Long userId, String memo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime suspendedUntil = now.plusDays(SUSPENSION_DAYS);
        User user = userAdaptor.findUserById(userId);
        if (user.getAccountState() == AccountState.SUSPENDED) {
            throw UserAlreadySuspendedException.EXCEPTION;
        }
        user.suspend(suspendedUntil);
        userAccessRevokedPublisher.publishSuspended(userId, suspendedUntil);
        saveManualSanctionHistory(userId, adminId, UserSanctionType.SUSPENDED, now, suspendedUntil, memo);
    }

    private void withdrawUserManually(Long adminId, Long userId, String memo) {
        authService.withDrawUser(
                userId,
                Set.of(WithdrawReason.OTHER),
                StringUtils.hasText(memo) ? memo.trim() : ACCOUNT_DELETION_DETAIL
        );
        saveManualSanctionHistory(userId, adminId, UserSanctionType.WITHDRAWN, LocalDateTime.now(), null, memo);
    }

    private void restoreUserManually(Long adminId, Long userId, String memo) {
        User user = userAdaptor.findUserById(userId);
        if (user.getAccountState() == AccountState.DELETED) {
            throw InvalidWithdrawException.EXCEPTION;
        }
        if (user.getAccountState() != AccountState.SUSPENDED) {
            throw UserNotSuspendedException.EXCEPTION;
        }
        LocalDateTime now = LocalDateTime.now();
        user.restore();
        userBlacklistAdaptor.unblock(userId);
        saveManualSanctionHistory(userId, adminId, UserSanctionType.RESTORED, now, null, memo);
    }

    private void deleteContentManually(
            Long adminId,
            Long userId,
            TargetContentType targetType,
            Long targetId,
            String memo
    ) {
        if (targetType == null || targetId == null) {
            throw InvalidAdminUserSanctionRequestException.EXCEPTION;
        }

        userAdaptor.findUserById(userId);

        switch (targetType) {
            case FEED -> deleteFeedManually(userId, targetId);
            case FEED_REPLY -> deleteFeedReplyManually(userId, targetId);
            case REVIEW -> deleteReviewManually(userId, targetId);
            case TOPIC_ROOM -> throw InvalidAdminUserSanctionRequestException.EXCEPTION;
            case CHAT -> deleteChatMessageManually(userId, targetId);
        }

        saveManualSanctionHistory(
                userId,
                adminId,
                UserSanctionType.CONTENT_DELETED,
                LocalDateTime.now(),
                null,
                memo
        );
    }

    private void deleteFeedManually(Long userId, Long boardId) {
        validateContentOwner(userId, readerFeedAdaptor.findBoardOwnerUserId(boardId));
        Long ownerId = boardAdaptor.adminDeleteReaderBoard(boardId);
        if (ownerId != null) {
            libraryAdaptor.decrementBoardCount(ownerId);
        }
    }

    private void deleteFeedReplyManually(Long userId, Long replyId) {
        validateContentOwner(userId, readerFeedAdaptor.findReplyOwnerUserId(replyId));
        readerFeedAdaptor.adminDeleteReaderBoardReply(replyId);
    }

    private void deleteReviewManually(Long userId, Long reviewId) {
        validateContentOwner(userId, reviewAdaptor.findReviewerIdById(reviewId));
        if (!reviewAdaptor.adminDeleteReview(reviewId)) return;

        ReviewedWorksIdAndRatingInfo info = reviewAdaptor.getReviewedWorksIdAndRatingInfo(reviewId);
        reviewLikeAdaptor.deleteAllRelatedReviewLike(reviewId);
        worksAdaptor.updateDecrementingReviewInfo(info.worksId(), info.rating().getRatingValue());
        libraryAdaptor.decrementReviewCount(userId);
    }

    private void deleteChatMessageManually(Long userId, Long messageId) {
        int deletedCount = chatAdaptor.softDeleteTalkMessageBySender(messageId, userId);
        if (deletedCount == 0) {
            throw InvalidAdminUserSanctionRequestException.EXCEPTION;
        }
    }

    private void validateContentOwner(Long userId, Long ownerId) {
        if (!userId.equals(ownerId)) {
            throw ForbiddenApproachException.EXCEPTION;
        }
    }

    private void saveManualSanctionHistory(
            Long userId,
            Long adminId,
            UserSanctionType type,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            String memo
    ) {
        userSanctionHistoryAdaptor.save(UserSanctionHistory.builder()
                .userId(userId)
                .adminId(adminId)
                .type(type)
                .source(UserSanctionSource.MANUAL)
                .reportCaseId(null)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .memo(StringUtils.hasText(memo) ? memo.trim() : null)
                .build());
    }

}
