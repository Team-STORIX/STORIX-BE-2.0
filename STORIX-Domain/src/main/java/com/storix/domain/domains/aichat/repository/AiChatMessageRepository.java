package com.storix.domain.domains.aichat.repository;

import com.storix.domain.domains.aichat.domain.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {
}
