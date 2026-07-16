package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.UserAppEvent;
import com.storix.domain.domains.event.domain.UserAppEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAppEventRepository extends JpaRepository<UserAppEvent, Long> {

    List<UserAppEvent> findAllByUserIdAndStatusOrderByIdAsc(Long userId, UserAppEventStatus status, Pageable pageable);
}
