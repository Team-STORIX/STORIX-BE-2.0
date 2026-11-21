package com.storix.domain.domains.aichat.repository;

import com.storix.domain.domains.aichat.domain.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiChatRepository extends JpaRepository<AiChat, Long> {

    Optional<AiChat> findByUserId(Long userId);
}
