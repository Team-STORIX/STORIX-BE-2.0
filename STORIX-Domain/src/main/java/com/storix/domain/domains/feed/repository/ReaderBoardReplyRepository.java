package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.ReaderBoardReply;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReaderBoardReplyRepository extends JpaRepository<ReaderBoardReply, Long> {

    // 피드 댓글 좋아요
    boolean existsByIdAndBoard_Id(Long replyId, Long boardId);

    Optional<ReaderBoardReply> findByIdAndBoard_Id(Long replyId, Long boardId);

    // 댓글 작성자 userId 단건 조회 (boardId 일치 검증 포함)
    @Query("SELECT r.userId FROM ReaderBoardReply r WHERE r.id = :replyId AND r.board.id = :boardId")
    Optional<Long> findUserIdByIdAndBoardId(@Param("replyId") Long replyId, @Param("boardId") Long boardId);

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

    // 답댓글 childReplyCount
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

    boolean existsByIdAndBoard_IdAndDeletedFalse(Long id, Long boardId);

    @Query("SELECT r.userId FROM ReaderBoardReply r WHERE r.id = :replyId AND r.board.id = :boardId AND r.deleted = false")
    Optional<Long> findActiveUserIdByIdAndBoardId(@Param("replyId") Long replyId, @Param("boardId") Long boardId);

    // 피드 댓글 조회 (최상위 댓글 + 답댓글 fetch join) — 삭제된 댓글 제외
    // FETCH JOIN with ON clause is invalid in JPQL; deleted filter applied via @SQLRestriction on childReplies
    @Query("SELECT r FROM ReaderBoardReply r " +
            "LEFT JOIN FETCH r.childReplies " +
            "WHERE r.board.id = :boardId AND r.parentReply IS NULL AND r.deleted = false")
    Slice<ReaderBoardReply> findAllByBoard_Id(@Param("boardId") Long boardId, Pageable pageable);

    // 차단 유저 제외 댓글 조회
    @Query("SELECT DISTINCT r FROM ReaderBoardReply r " +
            "LEFT JOIN FETCH r.childReplies c " +
            "WHERE r.board.id = :boardId AND r.parentReply IS NULL AND r.userId NOT IN :blockedIds")
    Slice<ReaderBoardReply> findAllByBoard_IdExcludingBlocked(
            @Param("boardId") Long boardId,
            @Param("blockedIds") List<Long> blockedIds,
            Pageable pageable);

    // 답댓글 조회
    @Query("SELECT r FROM ReaderBoardReply r " +
            "WHERE r.parentReply.id = :parentReplyId " +
            "ORDER BY r.createdAt ASC")
    Slice<ReaderBoardReply> findAllByParentReplyId(@Param("parentReplyId") Long parentReplyId, Pageable pageable);

    // 프로필 댓글 조회 (삭제된 댓글 제외)
    @Query("SELECT r FROM ReaderBoardReply r WHERE r.userId = :userId AND r.deleted = false ORDER BY r.createdAt DESC")
    Slice<ReaderBoardReply> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    long countByUserIdAndDeletedFalse(Long userId);

    // 관리자 댓글 강제 삭제 (이미 삭제된 댓글이면 0건 반영, 중복 카운트 감소 방지)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderBoardReply r " +
            "SET r.deleted = true, r.deletedBy = com.storix.domain.domains.plus.domain.DeletedBy.ADMIN, r.deletedAt = :now " +
            "WHERE r.id = :replyId AND r.deleted = false")
    int softDeleteByAdminIfNotDeleted(@Param("replyId") Long replyId, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReaderBoardReply r WHERE r.deleted = true AND r.deletedAt < :cutoff")
    int hardDeleteBefore(@Param("cutoff") LocalDateTime cutoff);

}
