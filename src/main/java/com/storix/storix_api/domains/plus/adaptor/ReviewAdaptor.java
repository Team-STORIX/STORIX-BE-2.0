package com.storix.storix_api.domains.plus.adaptor;

import com.storix.storix_api.domains.plus.domain.Rating;
import com.storix.storix_api.domains.plus.domain.Review;
import com.storix.storix_api.domains.plus.dto.*;
import com.storix.storix_api.domains.plus.repository.ReviewRepository;
import com.storix.storix_api.domains.review.controller.dto.ModifyReviewRequest;
import com.storix.storix_api.global.apiPayload.exception.plus.DuplicateReviewUploadException;
import com.storix.storix_api.global.apiPayload.exception.works.InvalidReviewDeleteRequestException;
import com.storix.storix_api.global.apiPayload.exception.works.InvalidReviewUpdateRequestException;
import com.storix.storix_api.global.apiPayload.exception.works.UnknownReviewException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewAdaptor {

    private final ReviewRepository reviewRepository;

    // 플러스탭
    public Review saveReview(CreateReviewCommand cmd) {
        try {
            return reviewRepository.save(cmd.toEntity());
        } catch (DataIntegrityViolationException e) {
            throw DuplicateReviewUploadException.EXCEPTION;
        }
    }

    public void existsByUserAndWorks(Long userId, Long worksId) {
        boolean isReviewExist = reviewRepository.existsByLibraryUserIdAndWorksId(userId, worksId);
        if (isReviewExist) {
            throw DuplicateReviewUploadException.EXCEPTION;
        }
    }

    // 서재탭
    public Slice<ReviewedWorksIdAndRatingInfo> getWorksListByUserId(Long userId, Pageable pageable) {
        return reviewRepository.findWorksIdsByUserId(userId, pageable);
    }

    public List<ReviewedWorksIdAndRatingInfo> findAllWorksIdsByUserId(Long userId) {
        return reviewRepository.findAllWorksIdsByUserId(userId);
    }

    public List<ReviewedWorksIdAndRatingInfo> findAllReviewInfoByFavoriteWorks(Long userId, List<Long> worksIds) {
        if (worksIds == null || worksIds.isEmpty()) {
            return Collections.emptyList();
        }

        return reviewRepository.findAllReviewInfoByFavoriteWorks(userId, worksIds);
    }

    // 작품 상세탭
    public long getReviewCount(Long worksId) {
        return reviewRepository.countByWorksId(worksId);
    }

    public boolean isMyReviewExist(Long userId, Long worksId) {
        return reviewRepository.existsByLibraryUserIdAndWorksId(userId, worksId);
    }

    public SliceReviewInfo getMyReviewInfo(Long userId, Long worksId) {
        return reviewRepository.findMySliceReviewInfo(userId, worksId);
    }

    public Slice<SliceReviewInfo> getOtherReviewInfo(Long userId, Long worksId, Pageable pageable) {
        return reviewRepository.findOtherSliceReviewInfo(userId, worksId, pageable);
    }

    public ReviewInfo findReviewById(Long reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            return ReviewInfo.of(review);
        } else {
            throw UnknownReviewException.EXCEPTION;
        }
    }

    public Long findReviewerIdById(Long reviewId) {
        Optional<Long> reviewerId = reviewRepository.findLibraryUserIdById(reviewId);
        if (reviewerId.isPresent()) {
            return reviewerId.get();
        } else {
            throw UnknownReviewException.EXCEPTION;
        }
    }

    public Long updateReviewDetail(Long reviewId, ModifyReviewRequest req) {
        int isUpdated = reviewRepository.updateMyReview(reviewId, req.rating(), req.isSpoiler(), req.content());
        if (isUpdated == 0) {
            throw InvalidReviewUpdateRequestException.EXCEPTION;
        }
        return reviewId;
    }

    public ReviewedWorksIdAndRatingInfo getReviewedWorksIdAndRatingInfo(Long reviewId) {
        Optional<ReviewedWorksIdAndRatingInfo> reviewInfo = reviewRepository.findWorksAndRatingInfo(reviewId);
        if (reviewInfo.isPresent()) {
            return reviewInfo.get();
        } else {
            throw UnknownReviewException.EXCEPTION;
        }
    }

    public void deleteReview(Long userId, Long reviewId) {
        int isDeleted = reviewRepository.deleteByIdAndUserId(reviewId, userId);
        if (isDeleted == 0) {
            throw InvalidReviewDeleteRequestException.EXCEPTION;
        }
    }

    // 프로필 탭
    public List<RatingCountInfo> countByRating(Long userId) {
        return reviewRepository.countByRating(userId);
    }

    public List<Long> findWorksIdsByHighRatings(Long userId) {
        return reviewRepository.findWorksIdsByRatings(
                userId, List.of(Rating.FIVE, Rating.FOUR_POINT_FIVE));
    }

}
