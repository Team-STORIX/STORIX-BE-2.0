package com.storix.storix_api.domains.favorite.repository;

import com.storix.storix_api.domains.favorite.domain.FavoriteWorks;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteWorksRepository extends JpaRepository<FavoriteWorks, Long> {

    // 관심 작품 등록 / 해제용
    void deleteByUserId(Long userId);

    boolean existsByUserIdAndWorksId(Long userId, Long worksId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FavoriteWorks f " +
            "WHERE f.userId = :userId AND f.worksId = :worksId ")
    int deleteSingleFavoriteWorks(@Param("userId") Long userId,
                                   @Param("worksId") Long worksId);

    // 관심 작품 조회용
    int countByUserId(Long userId);

    @Query("SELECT f.worksId FROM FavoriteWorks f " +
            "WHERE f.userId = :userId")
    Slice<Long> findWorksIdsByUserId(Long userId, Pageable pageable);

    // 선호 해시태그 용
    @Query("SELECT f.worksId FROM FavoriteWorks f " +
            "WHERE f.userId = :userId")
    List<Long> findAllWorksIdsByUserId(Long userId);

}
