package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.user.domain.UserTitleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserTitleHistoryRepository extends JpaRepository<UserTitleHistory, Long> {

    List<UserTitleHistory> findAllByUserIdInAndTitleIn(Collection<Long> userIds, Collection<Title> titles);
}
