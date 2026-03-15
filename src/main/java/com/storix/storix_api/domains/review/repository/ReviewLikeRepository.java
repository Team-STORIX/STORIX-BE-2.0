package com.storix.storix_api.domains.review.repository;

import com.storix.storix_api.domains.review.domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByUserIdAndReview_Id(Long userId, Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReviewLike rl " +
            "WHERE rl.userId = :userId AND rl.review.id = :reviewId ")
    int deleteLike(@Param("userId") Long userId, @Param("reviewId") Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReviewLike rl WHERE rl.review.id = :reviewId")
    void deleteAllByReviewId(@Param("reviewId") Long reviewId);

}
