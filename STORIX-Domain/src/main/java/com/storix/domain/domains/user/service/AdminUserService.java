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
import com.storix.domain.domains.user.dto.AdminUserContentPageResponse;
import com.storix.domain.domains.user.dto.AdminUserListResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionDetailResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionHistoryResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionPageResponse;
import com.storix.domain.domains.user.dto.AdminUserSearchCondition;
import com.storix.domain.domains.user.exception.admin.InvalidAdminUserSanctionRequestException;
import com.storix.domain.domains.user.exception.auth.ForbiddenApproachException;
import com.storix.domain.domains.user.exception.auth.InvalidWithdrawException;
import com.storix.domain.domains.user.exception.auth.SuspendedUserException;
import com.storix.domain.domains.user.publisher.UserAccessRevokedPublisher;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    public Page<AdminUserListResponse> searchUsers(AdminUserSearchCondition condition, Pageable pageable) {
        String nickName = StringUtils.hasText(condition.nickName()) ? condition.nickName().trim() : null;
        return userAdaptor.findAdminUsers(
                condition.userId(),
                nickName,
                condition.accountState(),
                pageable
        ).map(user -> new AdminUserListResponse(
                user.userId(),
                user.nickName(),
                user.email(),
                user.oauthProvider(),
                user.joinedAt(),
                user.accountState(),
                user.suspendedUntil(),
                user.lastLoginAt(),
                countReportedByUserId(user.userId())
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
        List<AdminUserContentItemResponse> contents = new java.util.ArrayList<>();
        Pageable unpaged = Pageable.unpaged();
        contents.addAll(boardAdaptor.findAdminBoardContentsByUserId(userId, unpaged).getContent());
        contents.addAll(readerFeedAdaptor.findAdminReplyContentsByUserId(userId, unpaged).getContent());
        contents.addAll(chatAdaptor.findAdminChatContentsByUserId(userId, unpaged).getContent());
        contents.addAll(reviewAdaptor.findAdminReviewContentsByUserId(userId, unpaged).getContent());

        List<AdminUserContentItemResponse> sortedContents = contents.stream()
                .sorted(Comparator.comparing(AdminUserContentItemResponse::createdAt).reversed())
                .toList();

        int start = Math.min((int) pageable.getOffset(), sortedContents.size());
        int end = Math.min(start + pageable.getPageSize(), sortedContents.size());
        return AdminUserContentPageResponse.from(new PageImpl<>(
                sortedContents.subList(start, end),
                pageable,
                sortedContents.size()
        ));
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
            throw SuspendedUserException.EXCEPTION;
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
