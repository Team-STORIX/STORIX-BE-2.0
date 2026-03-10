package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.ReaderBoardReplyLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReaderBoardReplyLikeRepository extends JpaRepository<ReaderBoardReplyLike, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReaderBoardReplyLike rpl " +
            "WHERE rpl.userId = :userId AND rpl.reply.id = :replyId ")
    int deleteLike(@Param("userId") Long userId, @Param("replyId") Long replyId);

    @Query("SELECT l.reply.id " +
            "FROM ReaderBoardReplyLike l " +
            "WHERE l.userId = :userId AND l.reply.id IN :replyIds")
    List<Long> findLikedReplyIds(@Param("userId") Long userId,
                                 @Param("replyIds") List<Long> replyIds);

}
