package com.storix.storix_api.domains.works.repository;

import com.storix.storix_api.domains.onboarding.dto.OnboardingWorksInfo;
import com.storix.storix_api.domains.works.domain.Works;
import com.storix.storix_api.domains.works.dto.LibraryWorksInfo;
import com.storix.storix_api.domains.works.dto.SlicedWorksInfo;
import com.storix.storix_api.domains.works.dto.TopicRoomWorksInfo;
import com.storix.storix_api.domains.works.dto.WorksInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorksRepository extends JpaRepository<Works, Long> {

    @Query("SELECT w FROM Works w " +
            "WHERE ( w.worksName LIKE %:keyword% " +
            "OR w.author LIKE %:keyword% " +
            "OR w.illustrator LIKE %:keyword% " +
            "OR w.originalAuthor LIKE %:keyword% ) " +
            "AND w.ageClassification <> com.storix.storix_api.domains.works.domain.AgeClassification.AGE_18 ")
    Slice<Works> findBySearchKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT (COUNT(w) > 0) FROM Works w " +
            "WHERE w.author = :nickName " +
            "   OR w.illustrator = :nickName " +
            "   OR w.originalAuthor = :nickName")
    boolean existsByAnyAuthorName(@Param("nickName") String nickName);

    @Query("SELECT w.id FROM Works w " +
            "WHERE w.worksName LIKE %:keyword% " +
            "OR w.author LIKE %:keyword% " +
            "OR w.illustrator LIKE %:keyword% " +
            "OR w.originalAuthor LIKE %:keyword%")
    List<Long> findAllIdsByKeyword(@Param("keyword") String keyword);

    @Query("SELECT w FROM Works w " +
            "LEFT JOIN FETCH w.hashtags " +
            "WHERE w.id = :worksId")
    Optional<Works> findByIdWithHashtags(@Param("worksId") Long worksId);

    @Query("SELECT (w.ageClassification = com.storix.storix_api.domains.works.domain.AgeClassification.AGE_18) " +
            "FROM Works w " +
            "WHERE w.id = :worksId")
    Boolean isWorksForAdult(@Param("worksId") Long worksId);

    // 리뷰 관련 정보 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Works w
        SET
          w.avgRating =
            (COALESCE(w.avgRating, 0.0) * COALESCE(w.reviewsCount, 0) + :newRating)
            / (COALESCE(w.reviewsCount, 0) + 1),
          w.reviewsCount = COALESCE(w.reviewsCount, 0) + 1
        WHERE w.id = :worksId
    """)
    void incrementReviewsCountAndUpdateAverageRating(
            @Param("worksId") Long worksId,
            @Param("newRating") double newRating
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Works w
        SET
          w.avgRating =
            CASE
              WHEN COALESCE(w.reviewsCount, 0) <= 1 THEN NULL
              ELSE
                (COALESCE(w.avgRating, 0.0) * COALESCE(w.reviewsCount, 0) - :deletedRating)
                / (COALESCE(w.reviewsCount, 0) - 1)
            END,
          w.reviewsCount =
            CASE
              WHEN COALESCE(w.reviewsCount, 0) <= 1 THEN 0
              ELSE COALESCE(w.reviewsCount, 0) - 1
            END
        WHERE w.id = :worksId
    """)
    void decrementReviewsCountAndUpdateAverageRating(
            @Param("worksId") Long worksId,
            @Param("deletedRating") double deletedRating
    );

    // 서재 관련 작품 정보 조회
    @Query("SELECT new com.storix.storix_api.domains.works.dto.LibraryWorksInfo(w.id, w.worksName, w.author, w.illustrator, w.originalAuthor, w.thumbnailUrl, w.worksType, w.genre) " +
            "FROM Works w " +
            "WHERE w.id IN :worksIds")
    List<LibraryWorksInfo> findLibraryWorksInfoByIds(@Param("worksIds") List<Long> worksIds);

    @Query("SELECT new com.storix.storix_api.domains.works.dto.LibraryWorksInfo(w.id, w.worksName, w.author, w.illustrator, w.originalAuthor, w.thumbnailUrl, w.worksType, w.genre) " +
            "FROM Works w " +
            "WHERE w.id IN :worksIds " +
            "AND w.worksName LIKE %:keyword% ")
    Slice<LibraryWorksInfo> searchLibraryWorksInfoByIds(@Param("worksIds") List<Long> worksIds,
                                                        @Param("keyword") String keyword,
                                                        Pageable pageable);

    // 작품 관련 정보 조회
    @Query("SELECT new com.storix.storix_api.domains.works.dto.WorksInfo(w.id, w.thumbnailUrl, w.worksName, w.artistName, w.worksType, w.genre) " +
            "FROM Works w " +
            "WHERE w.id IN :worksIds")
    List<WorksInfo> findWorksInfoByIds(@Param("worksIds") List<Long> worksIds);

    // 작품 상세 리뷰용
    @Query("SELECT new com.storix.storix_api.domains.works.dto.WorksInfo(w.id, w.thumbnailUrl, w.worksName, w.artistName, w.worksType, w.genre) " +
            "FROM Works w " +
            "WHERE w.id = :worksId")
    Optional<WorksInfo> findWorksInfoById(@Param("worksId") Long worksId);

    @Query("SELECT new com.storix.storix_api.domains.works.dto.TopicRoomWorksInfo(" +
            "w.id, w.worksName, w.thumbnailUrl, w.worksType) " +
            "FROM Works w " +
            "WHERE w.id IN :ids")
    List<TopicRoomWorksInfo> findSimpleInfoByIdIn(@Param("ids") List<Long> ids);

    // 온보딩 작품 리스트 조회용
    @Query("SELECT new com.storix.storix_api.domains.onboarding.dto.OnboardingWorksInfo(w.id, w.worksName, w.thumbnailUrl, w.author, w.illustrator, w.originalAuthor) " +
            "FROM Works w " +
            "WHERE w.isOnboarding = true " +
            "ORDER BY w.id ASC ")
    List<OnboardingWorksInfo> findAllOnboardingWorksInfo();

    // 피드 관심 작품 리스트 조회용
    @Query("SELECT new com.storix.storix_api.domains.works.dto.SlicedWorksInfo(w.id, w.thumbnailUrl, w.worksName) " +
            "FROM Works w " +
            "WHERE w.id IN :worksIds")
    List<SlicedWorksInfo> findAllSlicedWorksInfoByWorksIds(@Param("worksIds") List<Long> worksIds);


    @Query("SELECT w.id FROM Works w WHERE w.id NOT IN :ids")
    List<Long> findCandidateIdsExcluding(@Param("ids") List<Long> ids);

    @Query("SELECT w.id FROM Works w")
    List<Long> findAllCandidateIds();

    @Query("SELECT DISTINCT w FROM Works w " +
            "LEFT JOIN FETCH w.hashtags " +
            "WHERE w.id IN :targetIds")
    List<Works> findAllByIdWithHashtags(@Param("targetIds") List<Long> targetIds);
}