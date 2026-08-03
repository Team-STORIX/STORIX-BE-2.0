package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.AppEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppEventRepository extends JpaRepository<AppEvent, Long> {

    Page<AppEvent> findAllByOrderByIdDesc(Pageable pageable);

    // 이벤트명 검색 — keyword null이면 전체 조회
    @Query("SELECT e FROM AppEvent e WHERE (:keyword IS NULL OR e.name LIKE %:keyword%) ORDER BY e.id DESC")
    Page<AppEvent> searchByName(@Param("keyword") String keyword, Pageable pageable);
}
