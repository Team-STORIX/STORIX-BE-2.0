package com.storix.storix_api.domains.review.adaptor;

import com.storix.storix_api.domains.review.repository.ReviewLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewLikeAdaptor {

    private final ReviewLikeRepository reviewLikeRepository;

    public boolean isAlreadyLiked(Long userId, Long reviewId) {
        return reviewLikeRepository.existsByUserIdAndReview_Id(userId, reviewId);
    }


    // 리뷰와 연관관계 맺은 좋아요 삭제
    public void deleteAllRelatedReviewLike(Long reviewId) {
        reviewLikeRepository.deleteAllByReviewId(reviewId);
    }
}
