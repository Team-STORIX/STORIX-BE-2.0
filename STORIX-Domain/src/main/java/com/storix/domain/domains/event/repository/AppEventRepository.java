package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.AppEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppEventRepository extends JpaRepository<AppEvent, Long> {

    Page<AppEvent> findAllByOrderByIdDesc(Pageable pageable);
}
