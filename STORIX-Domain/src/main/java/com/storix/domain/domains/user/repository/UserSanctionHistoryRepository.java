package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.UserSanctionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSanctionHistoryRepository extends JpaRepository<UserSanctionHistory, Long> {

    List<UserSanctionHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Page<UserSanctionHistory> findPageByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 제재 종료일 기준 파기
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserSanctionHistory h WHERE COALESCE(h.endedAt, h.startedAt) < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}
