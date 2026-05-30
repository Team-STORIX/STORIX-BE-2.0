package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);

    List<UserBlock> findAllByBlockerId(Long blockerId);
}
