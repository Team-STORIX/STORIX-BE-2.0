package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.UserSanctionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSanctionHistoryRepository extends JpaRepository<UserSanctionHistory, Long> {

    List<UserSanctionHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Page<UserSanctionHistory> findPageByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
