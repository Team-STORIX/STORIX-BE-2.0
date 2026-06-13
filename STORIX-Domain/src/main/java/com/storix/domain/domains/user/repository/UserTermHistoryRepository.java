package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.UserTermHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermHistoryRepository extends JpaRepository<UserTermHistory, Long> {
}
