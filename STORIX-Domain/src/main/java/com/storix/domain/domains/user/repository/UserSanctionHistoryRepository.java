package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.UserSanctionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSanctionHistoryRepository extends JpaRepository<UserSanctionHistory, Long> {
}
