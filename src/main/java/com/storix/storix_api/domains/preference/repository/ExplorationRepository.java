package com.storix.storix_api.domains.preference.repository;

import com.storix.storix_api.domains.preference.domain.PreferenceExploration;
import com.storix.storix_api.domains.works.domain.Works;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExplorationRepository extends JpaRepository<PreferenceExploration, Long> {

    // 오늘 DB에 저장된 총 응답 수
    @Query("SELECT COUNT(pe) FROM PreferenceExploration pe WHERE pe.userId = :userId AND pe.createdAt >= :startOfDay")
    int countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    // 오늘 DB에 저장된 작품 리스트 (JOIN 포함)
    @Query("SELECT w FROM PreferenceExploration pe JOIN Works w ON pe.worksId = w.id " +
            "WHERE pe.userId = :userId AND pe.isLiked = :isLiked AND pe.createdAt >= :startOfDay")
    List<Works> findWorksByLikedStatusToday(@Param("userId") Long userId, @Param("isLiked") boolean isLiked, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT pe.worksId FROM PreferenceExploration pe " +
            "WHERE pe.userId = :userId AND pe.isLiked = :isLiked AND pe.createdAt >= :threshold")
    List<Long> findRespondedWorksIdsByStatusToday(
            @Param("userId") Long userId,
            @Param("isLiked") boolean isLiked,
            @Param("threshold") LocalDateTime threshold
    );

    // 마이페이지 누적 차트용
    @Query("SELECT w.genre, COUNT(w) FROM PreferenceExploration pe JOIN Works w ON pe.worksId = w.id " +
            "WHERE pe.userId = :userId AND pe.isLiked = true GROUP BY w.genre")
    List<Object[]> countLikedGenresByUserId(@Param("userId") Long userId);

    @Query("SELECT pe.worksId FROM PreferenceExploration pe WHERE pe.userId = :userId")
    List<Long> findRespondedWorksIdsByUserId(@Param("userId") Long userId);

    // 유저가 특정 작품에 대해 이미 응답했는지 여부 확인
    boolean existsByUserIdAndWorksId(Long userId, Long worksId);
}