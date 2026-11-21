package com.storix.domain.domains.aichat.repository;

import com.storix.domain.domains.aichat.domain.AiChatSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiChatSettingRepository extends JpaRepository<AiChatSetting, Long> {

    Optional<AiChatSetting> findByUserId(Long userId);
}
