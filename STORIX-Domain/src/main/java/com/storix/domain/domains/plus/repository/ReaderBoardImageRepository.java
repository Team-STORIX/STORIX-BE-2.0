package com.storix.domain.domains.plus.repository;

import com.storix.domain.domains.plus.domain.ReaderBoardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReaderBoardImageRepository extends JpaRepository<ReaderBoardImage, Long> {

    @Query("SELECT rbi " +
            "FROM ReaderBoardImage rbi " +
            "WHERE rbi.readerBoard.id IN :boardIds " +
            "ORDER BY rbi.readerBoard.id ASC, rbi.sortOrder ASC ")
    List<ReaderBoardImage> findAllByBoardIds(@Param("boardIds") List<Long> boardIds);

    @Query("SELECT rbi.imageObjectKey FROM ReaderBoardImage rbi WHERE rbi.readerBoard.id IN :boardIds")
    List<String> findObjectKeysByBoardIds(@Param("boardIds") List<Long> boardIds);

    // 하드 삭제 배치용 — 부모 게시글 벌크 삭제는 cascade 를 타지 않으므로 이미지 행을 먼저 지운다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ReaderBoardImage rbi WHERE rbi.readerBoard.id IN :boardIds")
    int hardDeleteByBoardIds(@Param("boardIds") List<Long> boardIds);

}
