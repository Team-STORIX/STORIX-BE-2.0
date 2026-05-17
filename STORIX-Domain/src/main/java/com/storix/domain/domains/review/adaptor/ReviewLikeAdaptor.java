package com.storix.domain.domains.review.adaptor;

import com.storix.domain.domains.plus.domain.Review;
import com.storix.domain.domains.plus.repository.ReviewRepository;
import com.storix.domain.domains.review.domain.ReviewLike;
import com.storix.domain.domains.review.dto.ReviewLikeToggleResponse;
import com.storix.domain.domains.review.repository.ReviewLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewLikeAdaptor {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;

    public boolean isAlreadyLiked(Long userId, Long reviewId) {
        return reviewLikeRepository.existsByUserIdAndReview_Id(userId, reviewId);
    }

    public int isReviewLikeDeleted(Long userId, Long reviewId) {
        return reviewLikeRepository.deleteLike(userId, reviewId);
    }

    public ReviewLikeToggleResponse deleteReviewLike(Long reviewId) {
        reviewRepository.decrementLikeCount(reviewId);
        return new ReviewLikeToggleResponse(false, reviewRepository.findLikeCountById(reviewId));
    }

    public ReviewLikeToggleResponse insertReviewLike(Long userId, Long reviewId) {
        try {
            Review reviewRef = reviewRepository.getReferenceById(reviewId);
            reviewLikeRepository.saveAndFlush(ReviewLike.of(userId, reviewRef));
            reviewRepository.incrementLikeCount(reviewId);
        } catch (DataIntegrityViolationException ignored) {
            // 이미 좋아요 상태로 간주
        }
        return new ReviewLikeToggleResponse(true, reviewRepository.findLikeCountById(reviewId));
    }

    // 리뷰와 연관관계 맺은 좋아요 삭제
    public void deleteAllRelatedReviewLike(Long reviewId) {
        reviewLikeRepository.deleteAllByReviewId(reviewId);
    }
}
