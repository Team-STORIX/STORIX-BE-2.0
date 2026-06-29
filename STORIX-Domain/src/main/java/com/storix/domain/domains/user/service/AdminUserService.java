package com.storix.domain.domains.user.service;

import com.storix.domain.domains.feed.adaptor.FeedReportAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
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
import com.storix.domain.domains.user.dto.AdminUserContentPageResponse;
import com.storix.domain.domains.user.dto.AdminUserContentType;
import com.storix.domain.domains.user.dto.AdminUserListResponse;
import com.storix.domain.domains.user.dto.AdminUserReportItemResponse;
import com.storix.domain.domains.user.dto.AdminUserReportPageResponse;
import com.storix.domain.domains.user.dto.AdminUserReportStats;
import com.storix.domain.domains.user.dto.AdminUserSanctionDetailResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionHistoryResponse;
import com.storix.domain.domains.user.dto.AdminUserSanctionPageResponse;
import com.storix.domain.domains.user.dto.AdminUserSearchCondition;
import com.storix.domain.domains.user.exception.auth.InvalidWithdrawException;
import com.storix.domain.domains.user.exception.auth.SuspendedUserException;
import com.storix.domain.domains.user.publisher.UserAccessRevokedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
        );
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
                reviewAdaptor.countActiveReviewsByUserId(userId)
        );
    }

    public AdminUserReportStats getReportStats(Long userId) {
        long reporterCount = feedReportAdaptor.countAllByReporterId(userId)
                + reviewReportAdaptor.countByReporterId(userId)
                + topicRoomReportAdaptor.countByReporterId(userId);
        long reportedCount = feedReportAdaptor.countAllByReportedUserId(userId)
                + reviewReportAdaptor.countByReportedUserId(userId)
                + topicRoomReportAdaptor.countByReportedUserId(userId);
        return new AdminUserReportStats(reporterCount, reportedCount);
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

    public AdminUserContentPageResponse getUserContents(Long userId, AdminUserContentType type, Pageable pageable) {
        userAdaptor.findUserById(userId);
        Page<com.storix.domain.domains.user.dto.AdminUserContentItemResponse> page = switch (type) {
            case BOARD -> boardAdaptor.findAdminBoardContentsByUserId(userId, pageable);
            case REPLY -> readerFeedAdaptor.findAdminReplyContentsByUserId(userId, pageable);
            case CHAT -> chatAdaptor.findAdminChatContentsByUserId(userId, pageable);
            case REVIEW -> reviewAdaptor.findAdminReviewContentsByUserId(userId, pageable);
        };
        return AdminUserContentPageResponse.from(page);
    }

    public AdminUserReportPageResponse getUserReportHistories(Long userId, Pageable pageable) {
        userAdaptor.findUserById(userId);
        List<AdminUserReportItemResponse> reports = new java.util.ArrayList<>();
        reports.addAll(feedReportAdaptor.findAdminReportsByReporterId(userId));
        reports.addAll(reviewReportAdaptor.findAdminReportsByReporterId(userId));
        reports.addAll(topicRoomReportAdaptor.findAdminReportsByReporterId(userId));
        reports.addAll(feedReportAdaptor.findAdminReportsByReportedUserId(userId));
        reports.addAll(reviewReportAdaptor.findAdminReportsByReportedUserId(userId));
        reports.addAll(topicRoomReportAdaptor.findAdminReportsByReportedUserId(userId));

        List<AdminUserReportItemResponse> sortedReports = reports.stream()
                .sorted(java.util.Comparator.comparing(AdminUserReportItemResponse::createdAt).reversed())
                .toList();
        int start = Math.min((int) pageable.getOffset(), sortedReports.size());
        int end = Math.min(start + pageable.getPageSize(), sortedReports.size());
        return AdminUserReportPageResponse.from(new PageImpl<>(
                sortedReports.subList(start, end),
                pageable,
                sortedReports.size()
        ));
    }

    @Transactional
    public void processManualSanction(
            Long adminId,
            Long userId,
            UserSanctionType type,
            String memo
    ) {
        switch (type) {
            case SUSPENDED -> suspendUserManually(adminId, userId, memo);
            case WITHDRAWN -> withdrawUserManually(adminId, userId, memo);
            case RESTORED -> restoreUserManually(adminId, userId, memo);
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
