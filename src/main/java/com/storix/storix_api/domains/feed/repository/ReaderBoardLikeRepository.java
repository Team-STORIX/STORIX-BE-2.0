package com.storix.storix_api.domains.feed.repository;

import com.storix.storix_api.domains.feed.domain.ReaderBoardLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReaderBoardLikeRepository extends JpaRepository<ReaderBoardLike, Long> {

    boolean existsByUserIdAndBoard_Id(Long userId, Long boardId);

    @Query("SELECT rl.board.id " +
            "FROM ReaderBoardLike rl " +
            "WHERE rl.userId = :userId " +
            "AND rl.board.id IN :boardIds")
    List<Long> findLikedBoardIds(@Param("userId") Long userId,
                                 @Param("boardIds") List<Long> boardIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReaderBoardLike rl " +
            "WHERE rl.userId = :userId AND rl.board.id = :boardId ")
    int deleteLike(@Param("userId") Long userId, @Param("boardId") Long boardId);


}
