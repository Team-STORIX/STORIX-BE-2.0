package com.storix.domain.domains.plus.repository;

import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.plus.domain.Review;
import com.storix.domain.domains.plus.dto.RatingCountInfo;
import com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo;
import com.storix.domain.domains.plus.dto.SliceReviewInfo;
import com.storix.domain.domains.user.dto.AdminUserContentItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 서재탭
    boolean existsByLibraryUserIdAndWorksIdAndDeletedFalse(Long libraryUserId, Long worksId);

    @Query("SELECT new com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo(r.worksId, r.id, r.rating) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId AND r.deleted = false")
    Slice<ReviewedWorksIdAndRatingInfo> findWorksIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo(r.worksId, r.id, r.rating) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId AND r.deleted = false")
    List<ReviewedWorksIdAndRatingInfo> findAllWorksIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT r.worksId " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId")
    List<Long> findAllReviewedWorksIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT new com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo(r.worksId, r.id, r.rating) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId AND r.worksId IN :worksIds AND r.deleted = false")
    List<ReviewedWorksIdAndRatingInfo> findAllReviewInfoByFavoriteWorks(@Param("userId") Long userId,
                                                                        @Param("worksIds") List<Long> worksIds);

    // 작품 상세탭
    @Query("SELECT COUNT(r) FROM Review r WHERE r.worksId = :worksId AND r.deleted = false")
    long countByWorksId(@Param("worksId") Long worksId);

    long countByLibraryUserIdAndDeletedFalse(Long libraryUserId);

    @Query("""
            SELECT new com.storix.domain.domains.user.dto.AdminUserContentItemResponse(
                r.id,
                com.storix.domain.domains.user.dto.AdminUserContentType.REVIEW,
                null,
                null,
                null,
                r.worksId,
                r.content,
                r.rating,
                null,
                r.likeCount,
                0,
                r.createdAt
            )
            FROM Review r
            WHERE r.libraryUserId = :userId AND r.deleted = false
            """)
    Page<AdminUserContentItemResponse> findAdminReviewContentsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT new com.storix.domain.domains.plus.dto.SliceReviewInfo(r.libraryUserId, r.id, r.isSpoiler, r.spoilerScript, r.content, r.rating, r.likeCount) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId AND r.worksId = :worksId AND r.deleted = false")
    SliceReviewInfo findMySliceReviewInfo(@Param("userId") Long userId,
                                          @Param("worksId") Long worksId);

    @Query("SELECT new com.storix.domain.domains.plus.dto.SliceReviewInfo(r.libraryUserId, r.id, r.isSpoiler, r.spoilerScript, r.content, r.rating, r.likeCount) " +
            "FROM Review r " +
            "WHERE (:userId IS NULL OR r.libraryUserId <> :userId) AND r.worksId = :worksId AND r.deleted = false")
    Slice<SliceReviewInfo> findOtherSliceReviewInfo(@Param("userId") Long userId,
                                                    @Param("worksId") Long worksId,
                                                    Pageable pageable);

    @Query("SELECT new com.storix.domain.domains.plus.dto.SliceReviewInfo(r.libraryUserId, r.id, r.isSpoiler, r.spoilerScript, r.content, r.rating, r.likeCount) " +
            "FROM Review r " +
            "WHERE (:userId IS NULL OR r.libraryUserId <> :userId) AND r.worksId = :worksId AND r.libraryUserId NOT IN :blockedIds")
    Slice<SliceReviewInfo> findOtherSliceReviewInfoExcludingBlocked(@Param("userId") Long userId,
                                                                    @Param("worksId") Long worksId,
                                                                    @Param("blockedIds") List<Long> blockedIds,
                                                                    Pageable pageable);

    Optional<Review> findById(Long reviewId);

    Optional<Review> findByIdAndDeletedFalse(Long id);

    @Query("SELECT r.libraryUserId " +
            "FROM Review r " +
            "WHERE r.id = :reviewId")
    Optional<Long> findLibraryUserIdById(@Param("reviewId") Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r " +
            "SET r.rating = :rating, r.isSpoiler = :isSpoiler, r.spoilerScript = :spoilerScript, r.content = :content " +
            "WHERE r.id = :reviewId")
    int updateMyReview(@Param("reviewId") Long reviewId,
                       @Param("rating") Rating rating,
                       @Param("isSpoiler") boolean isSpoiler,
                       @Param("spoilerScript") String spoilerScript,
                       @Param("content") String content);

    @Query("SELECT new com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo(r.worksId, r.id, r.rating) " +
            "FROM Review r " +
            "WHERE r.id = :reviewId")
    Optional<ReviewedWorksIdAndRatingInfo> findWorksAndRatingInfo(@Param("reviewId") Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Review r " +
            "WHERE r.id = :reviewId AND r.libraryUserId = :userId")
    int deleteByIdAndUserId(@Param("reviewId") Long reviewId,
                            @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Review r WHERE r.deleted = true AND r.deletedAt < :cutoff")
    int hardDeleteBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r " +
            "SET r.likeCount = r.likeCount + 1 " +
            "WHERE r.id = :reviewId")
    void incrementLikeCount(@Param("reviewId") Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r " +
            "SET r.likeCount = CASE WHEN r.likeCount > 0 THEN r.likeCount - 1 ELSE 0 END " +
            "WHERE r.id = :reviewId ")
    void decrementLikeCount(@Param("reviewId") Long reviewId);

    @Query("SELECT r.likeCount " +
            "FROM Review r " +
            "WHERE r.id = :reviewId")
    int findLikeCountById(@Param("reviewId") Long reviewId);

    // 프로필 탭
    @Query("SELECT new com.storix.domain.domains.plus.dto.RatingCountInfo(r.rating, count(r)) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId AND r.deleted = false " +
            "GROUP BY r.rating ")
    List<RatingCountInfo> countByRating(@Param("userId") Long userId);

    @Query("SELECT r.worksId " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId " +
            "AND r.rating IN (:ratings) AND r.deleted = false")
    List<Long> findWorksIdsByRatings(@Param("userId") Long userId,
                                     @Param("ratings") List<Rating> ratings);

}
