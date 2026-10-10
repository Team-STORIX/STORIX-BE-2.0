package com.storix.domain.domains.favorite.repository;

import com.storix.domain.domains.favorite.domain.FavoriteWorks;
import com.storix.domain.domains.favorite.dto.FavoriteWorksWithCreatedAt;
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

    // 성인작품 포함하여 조회하며 isAdultOnly 필터링은 프론트에서 처리함
    @Query("SELECT f.worksId FROM FavoriteWorks f " +
            "WHERE f.userId = :userId")
    Slice<Long> findWorksIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 선호 해시태그 용
    @Query("SELECT f.worksId FROM FavoriteWorks f " +
            "WHERE f.userId = :userId")
    List<Long> findAllWorksIdsByUserId(Long userId);

    @Query("SELECT new com.storix.domain.domains.favorite.dto.FavoriteWorksWithCreatedAt(f.worksId, f.createdAt) " +
            "FROM FavoriteWorks f " +
            "WHERE f.userId = :userId")
    List<FavoriteWorksWithCreatedAt> findAllWithCreatedAtByUserId(@Param("userId") Long userId);

    /** 작품 병합 */
    @Query("SELECT e.userId FROM FavoriteWorks e WHERE e.worksId = :worksId")
    List<Long> findUserIdsByWorksId(@Param("worksId") Long worksId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FavoriteWorks e WHERE e.worksId = :worksId AND e.userId IN :userIds")
    int deleteByWorksIdAndUserIds(@Param("worksId") Long worksId, @Param("userIds") List<Long> userIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE FavoriteWorks e SET e.worksId = :toWorksId WHERE e.worksId = :fromWorksId")
    int moveWorks(@Param("fromWorksId") Long fromWorksId, @Param("toWorksId") Long toWorksId);
}
