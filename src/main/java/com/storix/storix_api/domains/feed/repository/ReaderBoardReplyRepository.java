package com.storix.storix_api.domains.feed.repository;

import com.storix.storix_api.domains.feed.domain.ReaderBoardReply;
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

    // 피드 댓글 - 조회
    Slice<ReaderBoardReply> findAllByBoard_Id(Long boardId, Pageable pageable);

    // 프로필 댓글 - 조회
    Slice<ReaderBoardReply> findAllByUserId(Long userId, Pageable pageable);

}
