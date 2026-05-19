package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
}
