package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedUserId(Long blockerId, Long blockedUserId);

    List<UserBlock> findAllByBlockerId(Long blockerId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserBlock b WHERE b.blockerId = :blockerId AND b.blockedUserId = :blockedUserId")
    int deleteByBlockerIdAndBlockedUserId(@Param("blockerId") Long blockerId, @Param("blockedUserId") Long blockedUserId);
}
