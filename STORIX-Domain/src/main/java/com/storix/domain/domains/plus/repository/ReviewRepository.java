package com.storix.domain.domains.plus.repository;

import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.plus.domain.Review;
import com.storix.domain.domains.plus.dto.RatingCountInfo;
import com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo;
import com.storix.domain.domains.plus.dto.SliceReviewInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 서재탭
    boolean existsByLibraryUserIdAndWorksId(Long libraryUserId, Long worksId);

    @Query("SELECT new com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo(r.worksId, r.id, r.rating) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId")
    Slice<ReviewedWorksIdAndRatingInfo> findWorksIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo(r.worksId, r.id, r.rating) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId")
    List<ReviewedWorksIdAndRatingInfo> findAllWorksIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT new com.storix.domain.domains.plus.dto.ReviewedWorksIdAndRatingInfo(r.worksId, r.id, r.rating) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId AND r.worksId IN :worksIds")
    List<ReviewedWorksIdAndRatingInfo> findAllReviewInfoByFavoriteWorks(@Param("userId") Long userId,
                                                                        @Param("worksIds") List<Long> worksIds);

    // 작품 상세탭
    long countByWorksId(Long worksId);

    @Query("SELECT new com.storix.domain.domains.plus.dto.SliceReviewInfo(r.libraryUserId, r.id, r.isSpoiler, r.content) " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId AND r.worksId = :worksId")
    SliceReviewInfo findMySliceReviewInfo(@Param("userId") Long userId,
                                          @Param("worksId") Long worksId);

    @Query("SELECT new com.storix.domain.domains.plus.dto.SliceReviewInfo(r.libraryUserId, r.id, r.isSpoiler, r.content) " +
            "FROM Review r " +
            "WHERE (:userId IS NULL OR r.libraryUserId <> :userId) AND r.worksId = :worksId")
    Slice<SliceReviewInfo> findOtherSliceReviewInfo(@Param("userId") Long userId,
                                                    @Param("worksId") Long worksId,
                                                    Pageable pageable);

    Optional<Review> findById(Long reviewId);

    @Query("SELECT r.libraryUserId " +
            "FROM Review r " +
            "WHERE r.id = :reviewId")
    Optional<Long> findLibraryUserIdById(@Param("reviewId") Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r " +
            "SET r.rating = :rating, r.isSpoiler = :isSpoiler, r.content = :content " +
            "WHERE r.id = :reviewId")
    int updateMyReview(@Param("reviewId") Long reviewId,
                       @Param("rating") Rating rating,
                       @Param("isSpoiler") boolean isSpoiler,
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
            "WHERE r.libraryUserId = :userId " +
            "GROUP BY r.rating ")
    List<RatingCountInfo> countByRating(@Param("userId") Long userId);


    @Query("SELECT r.worksId " +
            "FROM Review r " +
            "WHERE r.libraryUserId = :userId " +
            "AND r.rating IN (:ratings)")
    List<Long> findWorksIdsByRatings(@Param("userId") Long userId,
                                     @Param("ratings") List<Rating> ratings);

}