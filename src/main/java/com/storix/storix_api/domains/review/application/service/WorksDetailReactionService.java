package com.storix.storix_api.domains.review.application.service;

import com.storix.storix_api.domains.plus.domain.Review;
import com.storix.storix_api.domains.plus.repository.ReviewRepository;
import com.storix.storix_api.domains.review.domain.ReviewLike;
import com.storix.storix_api.domains.review.dto.ReviewLikeToggleResponse;
import com.storix.storix_api.domains.review.repository.ReviewLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorksDetailReactionService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Transactional
    public ReviewLikeToggleResponse toggleReviewLike(Long userId, Long reviewId) {

        int isDeleted = reviewLikeRepository.deleteLike(userId, reviewId);
        if (isDeleted == 1) {
            reviewRepository.decrementLikeCount(reviewId);

            int likeCount = reviewRepository.findLikeCountById(reviewId);
            return new ReviewLikeToggleResponse(false, likeCount);
        }

        try {
            Review reviewRef = reviewRepository.getReferenceById(reviewId);

            ReviewLike like = ReviewLike.of(userId, reviewRef);
            reviewLikeRepository.saveAndFlush(like);

            reviewRepository.incrementLikeCount(reviewId);

            int likeCount = reviewRepository.findLikeCountById(reviewId);
            return new ReviewLikeToggleResponse(true, likeCount);

        } catch (DataIntegrityViolationException e) {

            int likeCount = reviewRepository.findLikeCountById(reviewId);
            return new ReviewLikeToggleResponse(true, likeCount);
        }

    }

}
