package com.storix.domain.domains.library.repository;

import com.storix.domain.domains.library.domain.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibraryRepository extends JpaRepository<Library, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Library l SET l.reviewCount = l.reviewCount + 1 WHERE l.id = :id")
    void incrementReviewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Library l SET l.reviewCount = l.reviewCount - 1 " +
            "WHERE l.id = :id AND l.reviewCount > 0")
    int decrementReviewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Library l SET l.boardCount = l.boardCount + 1 WHERE l.id = :id")
    void incrementBoardCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Library l SET l.boardCount = l.boardCount - 1 " +
            "WHERE l.id = :id AND l.boardCount > 0")
    void decrementBoardCount(@Param("id") Long id);

    @Query("SELECT l.reviewCount FROM Library l " +
            "WHERE l.id = :userId")
    int findReviewCountByUserId(@Param("userId") Long userId);

}
