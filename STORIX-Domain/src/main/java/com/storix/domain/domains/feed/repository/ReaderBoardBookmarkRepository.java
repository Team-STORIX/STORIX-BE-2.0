package com.storix.domain.domains.feed.repository;

import com.storix.domain.domains.feed.domain.ReaderBoardBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReaderBoardBookmarkRepository extends JpaRepository<ReaderBoardBookmark, Long> {

    boolean existsByUserIdAndBoard_Id(Long userId, Long boardId);

    @Query("SELECT rb.board.id " +
            "FROM ReaderBoardBookmark rb " +
            "WHERE rb.userId = :userId " +
            "AND rb.board.id IN :boardIds")
    List<Long> findBookmarkedBoardIds(@Param("userId") Long userId,
                                      @Param("boardIds") List<Long> boardIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT IGNORE INTO reader_board_bookmark (user_id, reader_board_id) VALUES (:userId, :boardId)", nativeQuery = true)
    int insertBookmark(@Param("userId") Long userId, @Param("boardId") Long boardId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReaderBoardBookmark rb " +
            "WHERE rb.userId = :userId AND rb.board.id = :boardId ")
    int deleteBookmark(@Param("userId") Long userId, @Param("boardId") Long boardId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReaderBoardBookmark rb WHERE rb.board.id IN :boardIds")
    int hardDeleteByBoardIds(@Param("boardIds") List<Long> boardIds);
}
