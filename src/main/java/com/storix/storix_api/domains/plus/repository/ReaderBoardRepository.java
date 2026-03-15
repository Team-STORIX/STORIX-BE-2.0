package com.storix.storix_api.domains.plus.repository;

import com.storix.storix_api.domains.plus.domain.ReaderBoard;
import com.storix.storix_api.domains.plus.dto.StandardReaderBoardInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReaderBoardRepository extends JpaRepository<ReaderBoard, Long>, ReaderBoardRankingRepository {

    // 프로필 관련
    @Query("SELECT rb " +
            "FROM ReaderBoard rb " +
            "WHERE rb.userId = :userId ")
    Slice<ReaderBoard> findAllReaderBoardByUserId(Long userId, Pageable pageable);

    @Query("SELECT rb " +
            "FROM ReaderBoardLike rl " +
            "JOIN rl.board rb " +
            "WHERE rl.userId = :userId " +
            "ORDER BY rl.id DESC ")
    Slice<ReaderBoard> findAllLikedReaderBoards(@Param("userId") Long userId, Pageable pageable);

    // 홈 관련
    @Query("SELECT new com.storix.storix_api.domains.plus.dto.StandardReaderBoardInfo(rb.userId, rb.id, rb.content, rb.likeCount, rb.replyCount, rb.isSpoiler, rb.popularityScore) " +
            "FROM ReaderBoard rb " +
            "WHERE rb.createdAt > :threshold " +
            "ORDER BY COALESCE(rb.popularityScore, 0) DESC, rb.id DESC ")
    List<StandardReaderBoardInfo> findTop3TrendingFeed(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    @Query("SELECT new com.storix.storix_api.domains.plus.dto.StandardReaderBoardInfo(rb.userId, rb.id, rb.content, rb.likeCount, rb.replyCount, rb.isSpoiler, rb.popularityScore) " +
            "FROM ReaderBoard rb " +
            "ORDER BY COALESCE(rb.popularityScore, 0) DESC, rb.id DESC ")
    List<StandardReaderBoardInfo> findSteadyTrendingFeed(Pageable pageable);

    @Query("SELECT new com.storix.storix_api.domains.plus.dto.StandardReaderBoardInfo(rb.userId, rb.id, rb.content, rb.likeCount, rb.replyCount, rb.isSpoiler, rb.popularityScore) " +
            "FROM ReaderBoard rb " +
            "WHERE rb.id NOT IN :excludeIds " +
            "ORDER BY COALESCE(rb.popularityScore, 0) DESC, rb.id DESC ")
    List<StandardReaderBoardInfo> findSteadyTrendingFeedNotToday(@Param("excludeIds") List<Long> excludeIds, Pageable pageable);

    // 피드 관련
    @Query("SELECT rb " +
            "FROM ReaderBoard rb " +
            "WHERE rb.worksId = :worksId ")
    Slice<ReaderBoard> findAllReaderBoardByWorksId(Long worksId, Pageable pageable);

    Slice<ReaderBoard> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 피드 - 댓글
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

    // 피드 - 좋아요
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

}
