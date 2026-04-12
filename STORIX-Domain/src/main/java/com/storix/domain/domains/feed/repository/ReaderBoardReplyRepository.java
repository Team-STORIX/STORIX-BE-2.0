package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.ReaderBoardReply;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReaderBoardReplyRepository extends JpaRepository<ReaderBoardReply, Long> {

    // 피드 댓글 - 좋아요
    boolean existsByIdAndBoard_Id(Long replyId, Long boardId);

    @Query("SELECT r.likeCount " +
            "FROM ReaderBoardReply r " +
            "WHERE r.id = :replyId")
    int findLikeCountById(@Param("replyId") Long replyId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoardReply r " +
            "SET r.likeCount = r.likeCount + 1 " +
            "WHERE r.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoardReply r " +
            "SET r.likeCount = r.likeCount - 1 " +
            "WHERE r.id = :id AND r.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    // 답댓글 - childReplyCount
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoardReply r " +
            "SET r.childReplyCount = r.childReplyCount + 1 " +
            "WHERE r.id = :id")
    void incrementChildReplyCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoardReply r " +
            "SET r.childReplyCount = r.childReplyCount - 1 " +
            "WHERE r.id = :id AND r.childReplyCount > 0")
    void decrementChildReplyCount(@Param("id") Long id);

    // 피드 댓글 - 조회 (최상위 댓글 + 답댓글 fetch join)
    @Query("SELECT DISTINCT r FROM ReaderBoardReply r " +
            "LEFT JOIN FETCH r.childReplies c " +
            "WHERE r.board.id = :boardId AND r.parentReply IS NULL")
    Slice<ReaderBoardReply> findAllByBoard_Id(@Param("boardId") Long boardId, Pageable pageable);

    // 답댓글 조회
    @Query("SELECT r FROM ReaderBoardReply r " +
            "WHERE r.parentReply.id = :parentReplyId " +
            "ORDER BY r.createdAt ASC")
    Slice<ReaderBoardReply> findAllByParentReplyId(@Param("parentReplyId") Long parentReplyId, Pageable pageable);

    // 프로필 댓글 - 조회
    Slice<ReaderBoardReply> findAllByUserId(Long userId, Pageable pageable);

}
