package com.storix.domain.domains.review.service;

import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.review.adaptor.ReviewLikeAdaptor;
import com.storix.domain.domains.review.dto.ReviewLikeToggleResponse;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorksDetailReactionService {

    private final ReviewAdaptor reviewAdaptor;
    private final ReviewLikeAdaptor reviewLikeAdaptor;
    private final UserAdaptor userAdaptor;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    public ReviewLikeToggleResponse toggleReviewLike(Long userId, Long reviewId) {

        // 리뷰 작성자 조회
        Long reviewAuthorUserId = reviewAdaptor.findReviewerIdById(reviewId);

        int isDeleted = reviewLikeAdaptor.isReviewLikeDeleted(userId, reviewId);
        if (isDeleted == 1) {
            return reviewLikeAdaptor.deleteReviewLike(reviewId);
        }

        ReviewLikeToggleResponse response = reviewLikeAdaptor.insertReviewLike(userId, reviewId);
        publishReviewLikeNotification(userId, reviewId, reviewAuthorUserId);
        return response;
    }

    private void publishReviewLikeNotification(Long actorUserId, Long reviewId, Long reviewAuthorUserId) {
        StandardProfileInfo actor = userAdaptor.findStandardProfileInfoByUserId(actorUserId);
        notificationPublisher.publishUnlessSelf(
                actorUserId,
                NotificationEvent.likeReview(reviewAuthorUserId, reviewId, actor.nickName())
        );
    }
}
