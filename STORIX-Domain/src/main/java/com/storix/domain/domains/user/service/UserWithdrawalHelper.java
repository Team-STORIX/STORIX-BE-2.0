package com.storix.domain.domains.user.service;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.image.publisher.S3CleanupPublisher;
import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationSettingAdaptor;
import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserHistoryAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.domain.UserHistory;
import com.storix.domain.domains.user.domain.UserHistoryType;
import com.storix.domain.domains.user.domain.WithdrawReason;
import com.storix.domain.domains.user.publisher.UserAccessRevokedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserWithdrawalHelper {

    private final UserAdaptor userAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final LibraryAdaptor libraryAdaptor;
    private final PushDeviceAdaptor pushDeviceAdaptor;
    private final NotificationSettingAdaptor notificationSettingAdaptor;
    private final AdultVerificationAdaptor adultVerificationAdaptor;
    private final UserHistoryAdaptor userHistoryAdaptor;
    private final UserAccessRevokedPublisher userAccessRevokedPublisher;
    private final S3CleanupPublisher s3CleanupPublisher;

    // 본인 탈퇴 · 신고 처리 · 관리자 탈퇴 공용
    @Transactional
    public void withdraw(Long userId, Set<WithdrawReason> reasons, String detail) {
        // 1. 유저 soft-delete
        User user = userAdaptor.findUserByIdForUpdate(userId);
        String profileObjectKey = user.getProfileObjectKey();
        user.withdraw();
        s3CleanupPublisher.publish(profileObjectKey);

        // 2. 유저 관련 정보 (관심 작품, 서재) 삭제 + Redis 반영(refreshToken 삭제, blacklist 등록)은 커밋 후 처리
        userAccessRevokedPublisher.publishWithdrawn(userId);
        favoriteWorksAdaptor.deleteFavoriteWorks(userId);
        libraryAdaptor.deleteLibrary(userId);

        // 3. 푸시 알림 토큰 물리 삭제
        pushDeviceAdaptor.deleteAllByUserId(userId);

        // 4. 알림 설정 삭제 (재가입 시 새 row 생성됨)
        notificationSettingAdaptor.deleteByUserId(userId);

        // 5. 성인인증 이력 삭제
        adultVerificationAdaptor.deleteAllByUserId(userId);

        // 6. 탈퇴 사유 로그 저장
        if (reasons == null || reasons.isEmpty()) return;
        String otherDetail = (detail == null) ? null : detail.trim();
        LocalDateTime processedAt = LocalDateTime.now();
        for (WithdrawReason reason : reasons) {
            userHistoryAdaptor.save(UserHistory.builder()
                    .userId(userId)
                    .historyType(UserHistoryType.WITHDRAW)
                    .processor(STORIXStatic.UserHistory.PROCESSOR_TEAM_STORIX)
                    .processedAt(processedAt)
                    .reason(reason)
                    .detail(reason == WithdrawReason.OTHER ? otherDetail : null)
                    .build());
        }
    }
}
