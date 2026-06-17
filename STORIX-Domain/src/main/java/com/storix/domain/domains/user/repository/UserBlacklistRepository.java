package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.UserBlacklist;
import org.springframework.data.repository.CrudRepository;

public interface UserBlacklistRepository extends CrudRepository<UserBlacklist, Long> {
}
