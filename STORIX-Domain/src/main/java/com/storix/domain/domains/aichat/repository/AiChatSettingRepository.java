package com.storix.domain.domains.aichat.repository;

import com.storix.domain.domains.aichat.domain.AiChatSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiChatSettingRepository extends JpaRepository<AiChatSetting, Long> {

    Optional<AiChatSetting> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from AiChatSetting s where s.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
