package com.storix.domain.domains.plus.adaptor;

import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.plus.domain.Review;
import com.storix.domain.domains.plus.dto.*;
import com.storix.domain.domains.plus.repository.ReviewRepository;
import com.storix.domain.domains.review.dto.ModifyReviewRequest;
import com.storix.domain.domains.plus.exception.DuplicateReviewUploadException;
import com.storix.domain.domains.user.dto.AdminUserContentItemResponse;
import com.storix.domain.domains.works.exception.InvalidReviewDeleteRequestException;
import com.storix.domain.domains.works.exception.InvalidReviewUpdateRequestException;
import com.storix.domain.domains.works.exception.UnknownReviewException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
        boolean isReviewExist = reviewRepository.existsByLibraryUserIdAndWorksIdAndDeletedFalse(userId, worksId);
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

    public List<Long> findAllReviewedWorksIdsByUserId(Long userId) {
        return reviewRepository.findAllReviewedWorksIdsByUserId(userId);
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

    public long countActiveReviewsByUserId(Long userId) {
        return reviewRepository.countByLibraryUserIdAndDeletedFalse(userId);
    }

    public Page<AdminUserContentItemResponse> findAdminReviewContentsByUserId(Long userId, Pageable pageable) {
        return reviewRepository.findAdminReviewContentsByUserId(userId, pageable);
    }

    public boolean isMyReviewExist(Long userId, Long worksId) {
        return reviewRepository.existsByLibraryUserIdAndWorksIdAndDeletedFalse(userId, worksId);
    }

    public SliceReviewInfo getMyReviewInfo(Long userId, Long worksId) {
        return reviewRepository.findMySliceReviewInfo(userId, worksId);
    }

    public Slice<SliceReviewInfo> getOtherReviewInfo(Long userId, Long worksId, Pageable pageable) {
        return reviewRepository.findOtherSliceReviewInfo(userId, worksId, pageable);
    }

    public Slice<SliceReviewInfo> getOtherReviewInfoExcludingBlocked(Long userId, Long worksId, List<Long> blockedIds, Pageable pageable) {
        if (blockedIds.isEmpty()) {
            return reviewRepository.findOtherSliceReviewInfo(userId, worksId, pageable);
        }
        return reviewRepository.findOtherSliceReviewInfoExcludingBlocked(userId, worksId, blockedIds, pageable);
    }

    public ReviewInfo findReviewById(Long reviewId) {
        Review review = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> UnknownReviewException.EXCEPTION);
        return ReviewInfo.of(review);
    }

    public Long findReviewerIdById(Long reviewId) {
        Optional<Long> reviewerId = reviewRepository.findLibraryUserIdById(reviewId);
        if (reviewerId.isPresent()) {
            return reviewerId.get();
        } else {
            throw UnknownReviewException.EXCEPTION;
        }
    }

    public Long updateReviewDetail(Long reviewId, ModifyReviewRequest cmd) {
        String spoilerScript = cmd.isSpoiler() ? cmd.spoilerScript() : null;
        int isUpdated = reviewRepository.updateMyReview(reviewId, cmd.rating(), cmd.isSpoiler(), spoilerScript, cmd.content());
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

    // 관리자 리뷰 강제 삭제 (소유권 검증 없음, 원문 보존 soft delete, idempotent)
    // 이미 삭제된 경우 false 반환 → 호출자에서 카운트 감소 등 부수 효과 건너뜀
    public boolean adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> UnknownReviewException.EXCEPTION);
        return review.softDeleteByAdmin();
    }

    // 프로필 탭
    public List<RatingCountInfo> countByRating(Long userId) {
        return reviewRepository.countByRating(userId);
    }

    public List<Long> findWorksIdsByHighRatings(Long userId) {
        return reviewRepository.findWorksIdsByRatings(
                userId, List.of(Rating.FIVE, Rating.FOUR_POINT_FIVE));
    }

    public int hardDeleteBefore(LocalDateTime cutoff) {
        return reviewRepository.hardDeleteBefore(cutoff);
    }

}
