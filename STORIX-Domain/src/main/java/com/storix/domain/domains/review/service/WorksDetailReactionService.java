package com.storix.domain.domains.review.service;

import com.storix.domain.domains.plus.domain.Review;
import com.storix.domain.domains.plus.repository.ReviewRepository;
import com.storix.domain.domains.review.domain.ReviewLike;
import com.storix.domain.domains.review.dto.ReviewLikeToggleResponse;
import com.storix.domain.domains.review.repository.ReviewLikeRepository;
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
