package com.storix.storix_api.domains.plus.repository;

import com.storix.storix_api.domains.plus.domain.ReaderBoardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReaderBoardImageRepository extends JpaRepository<ReaderBoardImage, Long> {

    @Query("SELECT rbi " +
            "FROM ReaderBoardImage rbi " +
            "WHERE rbi.readerBoard.id IN :boardIds " +
            "ORDER BY rbi.readerBoard.id ASC, rbi.sortOrder ASC ")
    List<ReaderBoardImage> findAllByBoardIds(@Param("boardIds") List<Long> boardIds);

}
