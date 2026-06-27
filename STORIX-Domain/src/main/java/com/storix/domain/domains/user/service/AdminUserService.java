package com.storix.domain.domains.user.service;

import com.storix.domain.domains.feed.adaptor.FeedReportAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.review.adaptor.ReviewReportAdaptor;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomPersistenceAdapter;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomReportAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserSanctionHistoryAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.dto.AdminUserActivityStats;
import com.storix.domain.domains.user.dto.AdminUserBasicInfo;
import com.storix.domain.domains.user.dto.AdminUserListResponse;
import com.storix.domain.domains.user.dto.AdminUserReportStats;
import com.storix.domain.domains.user.dto.AdminUserSanctionHistoryResponse;
import com.storix.domain.domains.user.dto.AdminUserSearchCondition;
import com.storix.domain.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserAdaptor userAdaptor;
    private final BoardAdaptor boardAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final TopicRoomPersistenceAdapter topicRoomPersistenceAdapter;
    private final FeedReportAdaptor feedReportAdaptor;
    private final ReviewReportAdaptor reviewReportAdaptor;
    private final TopicRoomReportAdaptor topicRoomReportAdaptor;
    private final UserSanctionHistoryAdaptor userSanctionHistoryAdaptor;

    public Page<AdminUserListResponse> searchUsers(AdminUserSearchCondition condition, Pageable pageable) {
        String nickName = StringUtils.hasText(condition.nickName()) ? condition.nickName().trim() : null;
        return userRepository.searchAdminUsers(
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
                topicRoomPersistenceAdapter.countJoinedRooms(userId),
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
}
