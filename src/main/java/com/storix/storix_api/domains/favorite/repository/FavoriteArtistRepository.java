package com.storix.storix_api.domains.favorite.repository;

import com.storix.storix_api.domains.favorite.domain.FavoriteArtist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteArtistRepository extends JpaRepository<FavoriteArtist, Long> {

    // 관심 작가 등록 / 해제용
    boolean existsByUserIdAndArtistId(Long userId, Long artistId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FavoriteArtist f " +
            "WHERE f.userId = :userId AND f.artistId = :artistId ")
    int deleteSingleFavoriteArtist(@Param("userId") Long userId,
                                   @Param("artistId") Long artistId);

    // 관심 작가 조회용
    int countByUserId(Long userId);

    @Query("SELECT f.artistId FROM FavoriteArtist f " +
            "WHERE f.userId = :userId ")
    Slice<Long> findArtistIdsByUserId(@Param("userId") Long userId, Pageable pageable);

}
