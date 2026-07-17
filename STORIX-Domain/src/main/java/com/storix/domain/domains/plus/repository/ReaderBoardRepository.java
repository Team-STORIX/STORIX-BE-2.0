package com.storix.domain.domains.plus.repository;

import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.dto.StandardReaderBoardInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface ReaderBoardRepository extends JpaRepository<ReaderBoard, Long>, ReaderBoardRankingRepository {

    // 작성자 userId 단건 조회
    @Query("SELECT rb.userId FROM ReaderBoard rb WHERE rb.id = :boardId")
    Optional<Long> findUserIdById(@Param("boardId") Long boardId);

    // 프로필 관련
    @Query("SELECT rb " +
            "FROM ReaderBoard rb " +
            "WHERE rb.userId = :userId AND rb.deleted = false")
    Slice<ReaderBoard> findAllReaderBoardByUserId(Long userId, Pageable pageable);

    @Query("SELECT rb " +
            "FROM ReaderBoardLike rl " +
            "JOIN rl.board rb " +
            "WHERE rl.userId = :userId AND rb.deleted = false " +
            "ORDER BY rl.id DESC ")
    Slice<ReaderBoard> findAllLikedReaderBoards(@Param("userId") Long userId, Pageable pageable);

    // 홈 관련
    @Query("SELECT new com.storix.domain.domains.plus.dto.StandardReaderBoardInfo(rb.userId, rb.id, rb.content, rb.likeCount, rb.replyCount, rb.isSpoiler, rb.spoilerScript, rb.popularityScore) " +
            "FROM ReaderBoard rb " +
            "WHERE rb.createdAt > :threshold AND rb.deleted = false " +
            "ORDER BY COALESCE(rb.popularityScore, 0) DESC, rb.id DESC ")
    List<StandardReaderBoardInfo> findTop3TrendingFeed(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    @Query("SELECT new com.storix.domain.domains.plus.dto.StandardReaderBoardInfo(rb.userId, rb.id, rb.content, rb.likeCount, rb.replyCount, rb.isSpoiler, rb.spoilerScript, rb.popularityScore) " +
            "FROM ReaderBoard rb " +
            "WHERE rb.createdAt > :threshold AND rb.deleted = false " +
            "ORDER BY COALESCE(rb.popularityScore, 0) DESC, rb.id DESC ")
    List<StandardReaderBoardInfo> findSteadyTrendingFeed(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    @Query("SELECT new com.storix.domain.domains.plus.dto.StandardReaderBoardInfo(rb.userId, rb.id, rb.content, rb.likeCount, rb.replyCount, rb.isSpoiler, rb.spoilerScript, rb.popularityScore) " +
            "FROM ReaderBoard rb " +
            "WHERE rb.id NOT IN :excludeIds " +
            "AND rb.createdAt > :threshold AND rb.deleted = false " +
            "ORDER BY COALESCE(rb.popularityScore, 0) DESC, rb.id DESC ")
    List<StandardReaderBoardInfo> findSteadyTrendingFeedNotToday(@Param("excludeIds") List<Long> excludeIds, @Param("threshold") LocalDateTime threshold, Pageable pageable);

    // 피드 관련
    @Query("SELECT rb " +
            "FROM ReaderBoard rb " +
            "WHERE rb.worksId = :worksId AND rb.deleted = false")
    Slice<ReaderBoard> findAllReaderBoardByWorksId(Long worksId, Pageable pageable);

    @Query("SELECT rb " +
            "FROM ReaderBoard rb " +
            "WHERE rb.worksId = :worksId AND rb.userId NOT IN :blockedIds")
    Slice<ReaderBoard> findAllReaderBoardByWorksIdExcludingBlocked(
            @Param("worksId") Long worksId,
            @Param("blockedIds") List<Long> blockedIds,
            Pageable pageable);

    @Query("SELECT rb FROM ReaderBoard rb WHERE rb.deleted = false ORDER BY rb.createdAt DESC")
    Slice<ReaderBoard> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT rb FROM ReaderBoard rb WHERE rb.userId NOT IN :blockedIds ORDER BY rb.createdAt DESC")
    Slice<ReaderBoard> findAllExcludingBlockedOrderByCreatedAtDesc(
            @Param("blockedIds") List<Long> blockedIds,
            Pageable pageable);

    // 피드 댓글
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoard r " +
            "SET r.replyCount = r.replyCount + 1 " +
            "WHERE r.id = :id")
    void incrementReplyCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoard r " +
            "SET r.replyCount = r.replyCount - 1 " +
            "WHERE r.id = :id AND r.replyCount > 0")
    void decrementReplyCount(@Param("id") Long id);

    // 피드 좋아요
    @Query("SELECT r.likeCount " +
            "FROM ReaderBoard r " +
            "WHERE r.id = :boardId")
    int findLikeCountById(@Param("boardId") Long boardId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoard r " +
            "SET r.likeCount = r.likeCount + 1 " +
            "WHERE r.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoard r " +
            "SET r.likeCount = r.likeCount - 1 " +
            "WHERE r.id = :id AND r.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    Optional<ReaderBoard> findByIdAndDeletedFalse(Long id);

    boolean existsByIdAndDeletedFalse(Long id);

    @Query("SELECT r.id FROM ReaderBoard r WHERE r.deleted = true AND r.deletedAt < :cutoff")
    List<Long> findIdsForHardDelete(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReaderBoard r WHERE r.id IN :ids")
    int hardDeleteByIds(@Param("ids") List<Long> ids);

}
